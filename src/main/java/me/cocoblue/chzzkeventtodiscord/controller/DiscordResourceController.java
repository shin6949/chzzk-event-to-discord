package me.cocoblue.chzzkeventtodiscord.controller;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.PageResponseDto;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordBotProfileResponseDto;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordWebhookRequestDto;
import me.cocoblue.chzzkeventtodiscord.dto.discord.DiscordWebhookResponseDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.service.DiscordBotProfileResourceService;
import me.cocoblue.chzzkeventtodiscord.service.DiscordWebhookResourceService;
import me.cocoblue.chzzkeventtodiscord.service.StaticContentUrlResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/discord")
@RequiredArgsConstructor
public class DiscordResourceController {
    private final DiscordWebhookResourceService discordWebhookResourceService;
    private final DiscordBotProfileResourceService discordBotProfileResourceService;
    private final StaticContentUrlResolver staticContentUrlResolver;

    @GetMapping("/webhooks")
    public ResponseEntity<PageResponseDto<DiscordWebhookResponseDto>> listWebhooks(Pageable pageable, Authentication authentication) {
        final Page<DiscordWebhookResponseDto> response = discordWebhookResourceService
            .list(extractPrincipal(authentication), pageable)
            .map(DiscordWebhookResponseDto::fromEntity);
        return ResponseEntity.ok(PageResponseDto.from(response));
    }

    @GetMapping("/webhooks/{webhookId}")
    public ResponseEntity<DiscordWebhookResponseDto> getWebhook(
        @PathVariable Long webhookId,
        Authentication authentication
    ) {
        return ResponseEntity.ok(DiscordWebhookResponseDto.fromEntity(
            discordWebhookResourceService.get(webhookId, extractPrincipal(authentication))
        ));
    }

    @PostMapping("/webhooks")
    public ResponseEntity<DiscordWebhookResponseDto> createWebhook(
        @RequestBody DiscordWebhookRequestDto request,
        Authentication authentication
    ) {
        final DiscordWebhookResponseDto response = DiscordWebhookResponseDto.fromEntity(
            discordWebhookResourceService.create(request, extractPrincipal(authentication))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/webhooks/{webhookId}")
    public ResponseEntity<DiscordWebhookResponseDto> updateWebhook(
        @PathVariable Long webhookId,
        @RequestBody DiscordWebhookRequestDto request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(DiscordWebhookResponseDto.fromEntity(
            discordWebhookResourceService.update(webhookId, request, extractPrincipal(authentication))
        ));
    }

    @DeleteMapping("/webhooks/{webhookId}")
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long webhookId, Authentication authentication) {
        discordWebhookResourceService.delete(webhookId, extractPrincipal(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bot-profiles")
    public ResponseEntity<PageResponseDto<DiscordBotProfileResponseDto>> listBotProfiles(Pageable pageable, Authentication authentication) {
        final Page<DiscordBotProfileResponseDto> response = discordBotProfileResourceService
            .list(extractPrincipal(authentication), pageable)
            .map(entity -> DiscordBotProfileResponseDto.fromEntity(entity, staticContentUrlResolver));
        return ResponseEntity.ok(PageResponseDto.from(response));
    }

    @GetMapping("/bot-profiles/{botProfileId}")
    public ResponseEntity<DiscordBotProfileResponseDto> getBotProfile(
        @PathVariable Long botProfileId,
        Authentication authentication
    ) {
        return ResponseEntity.ok(DiscordBotProfileResponseDto.fromEntity(
            discordBotProfileResourceService.get(botProfileId, extractPrincipal(authentication)),
            staticContentUrlResolver
        ));
    }

    @PostMapping(value = "/bot-profiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiscordBotProfileResponseDto> createBotProfile(
        @RequestParam String alias,
        @RequestParam String username,
        @RequestParam(required = false) String ownerChannelId,
        @RequestPart MultipartFile avatar,
        Authentication authentication
    ) {
        final DiscordBotProfileResponseDto response = DiscordBotProfileResponseDto.fromEntity(
            discordBotProfileResourceService.create(alias, username, avatar, ownerChannelId, extractPrincipal(authentication)),
            staticContentUrlResolver
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/bot-profiles/{botProfileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiscordBotProfileResponseDto> updateBotProfile(
        @PathVariable Long botProfileId,
        @RequestParam(required = false) String alias,
        @RequestParam(required = false) String username,
        @RequestPart(required = false) MultipartFile avatar,
        Authentication authentication
    ) {
        return ResponseEntity.ok(DiscordBotProfileResponseDto.fromEntity(
            discordBotProfileResourceService.update(botProfileId, alias, username, avatar, extractPrincipal(authentication)),
            staticContentUrlResolver
        ));
    }

    @DeleteMapping("/bot-profiles/{botProfileId}")
    public ResponseEntity<Void> deleteBotProfile(@PathVariable Long botProfileId, Authentication authentication) {
        discordBotProfileResourceService.delete(botProfileId, extractPrincipal(authentication));
        return ResponseEntity.noContent().build();
    }

    private ChzzkPrincipal extractPrincipal(Authentication authentication) {
        final Object principalObject = authentication.getPrincipal();
        if (principalObject instanceof ChzzkPrincipal chzzkPrincipal) {
            return chzzkPrincipal;
        }

        final AppRole role = authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))
            ? AppRole.ADMIN
            : AppRole.USER;
        return new ChzzkPrincipal(authentication.getName(), role);
    }
}
