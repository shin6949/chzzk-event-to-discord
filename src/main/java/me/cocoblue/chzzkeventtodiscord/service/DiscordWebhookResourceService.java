package me.cocoblue.chzzkeventtodiscord.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormRepository;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataEntity;
import me.cocoblue.chzzkeventtodiscord.domain.discord.DiscordWebhookDataRepository;
import me.cocoblue.chzzkeventtodiscord.domain.soop.SoopSubscriptionRepository;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordWebhookRequestDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DiscordWebhookResourceService {
    private final DiscordWebhookDataRepository discordWebhookDataRepository;
    private final ChzzkChannelRepository chzzkChannelRepository;
    private final ChzzkSubscriptionFormRepository subscriptionFormRepository;
    private final SoopSubscriptionRepository soopSubscriptionRepository;

    @Transactional
    public Page<DiscordWebhookDataEntity> list(ChzzkPrincipal principal, Pageable pageable) {
        if (principal.role() == AppRole.ADMIN) {
            return discordWebhookDataRepository.findAll(pageable);
        }
        return discordWebhookDataRepository.findAllByOwnerId_ChannelId(principal.channelId(), pageable);
    }

    @Transactional
    public DiscordWebhookDataEntity get(Long id, ChzzkPrincipal principal) {
        final DiscordWebhookDataEntity entity = findReadable(id, principal);
        ensureReadable(entity, principal);
        return entity;
    }

    @Transactional
    public DiscordWebhookDataEntity create(DiscordWebhookRequestDto request, ChzzkPrincipal principal) {
        final String ownerChannelId = resolveOwnerChannelId(request.getOwnerChannelId(), principal, false);
        final ChzzkChannelEntity owner = resolveOwner(ownerChannelId);
        final String alias = requireText(request.getAlias(), "alias");
        final String url = requireUrl(request.getUrl());

        final DiscordWebhookDataEntity entity = DiscordWebhookDataEntity.builder()
            .name(alias)
            .webhookUrl(url)
            .ownerId(owner)
            .build();
        return discordWebhookDataRepository.saveAndFlush(entity);
    }

    @Transactional
    public DiscordWebhookDataEntity update(Long id, DiscordWebhookRequestDto request, ChzzkPrincipal principal) {
        final DiscordWebhookDataEntity entity = findReadable(id, principal);
        ensureWritable(entity, principal);

        if (StringUtils.hasText(request.getAlias())) {
            entity.setName(request.getAlias().trim());
        }
        if (StringUtils.hasText(request.getUrl())) {
            entity.setWebhookUrl(requireUrl(request.getUrl()));
        }
        return discordWebhookDataRepository.saveAndFlush(entity);
    }

    @Transactional
    public void delete(Long id, ChzzkPrincipal principal) {
        final DiscordWebhookDataEntity entity = findReadable(id, principal);
        ensureWritable(entity, principal);
        if (subscriptionFormRepository.existsByWebhookId_Id(id) || soopSubscriptionRepository.existsByWebhook_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "webhook is used by subscriptions");
        }
        discordWebhookDataRepository.delete(entity);
    }

    private DiscordWebhookDataEntity findReadable(Long id, ChzzkPrincipal principal) {
        if (principal.role() == AppRole.ADMIN) {
            return discordWebhookDataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "webhook not found"));
        }
        return discordWebhookDataRepository.findByIdAndOwnerId_ChannelId(id, principal.channelId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "webhook not found"));
    }

    private void ensureReadable(DiscordWebhookDataEntity entity, ChzzkPrincipal principal) {
        if (principal.role() == AppRole.ADMIN) {
            return;
        }
        if (!Objects.equals(entity.getOwnerId().getChannelId(), principal.channelId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "webhook does not belong to authenticated user");
        }
    }

    private void ensureWritable(DiscordWebhookDataEntity entity, ChzzkPrincipal principal) {
        ensureReadable(entity, principal);
    }

    private String resolveOwnerChannelId(String requestedOwnerChannelId, ChzzkPrincipal principal, boolean isUpdate) {
        if (principal.role() == AppRole.ADMIN) {
            if (StringUtils.hasText(requestedOwnerChannelId)) {
                return requestedOwnerChannelId.trim();
            }
            if (isUpdate) {
                return null;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ownerChannelId is required");
        }
        if (StringUtils.hasText(requestedOwnerChannelId) && !Objects.equals(requestedOwnerChannelId.trim(), principal.channelId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ownerChannelId does not match authenticated user");
        }
        return principal.channelId();
    }

    private ChzzkChannelEntity resolveOwner(String ownerChannelId) {
        return chzzkChannelRepository.findById(ownerChannelId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner channel not found"));
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private String requireUrl(String value) {
        final String url = requireText(value, "url");
        try {
            final URI uri = new URI(url);
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url must be http or https");
            }
            return url;
        } catch (URISyntaxException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url is invalid", exception);
        }
    }
}
