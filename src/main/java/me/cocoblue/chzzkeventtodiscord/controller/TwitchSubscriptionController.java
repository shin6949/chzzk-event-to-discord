package me.cocoblue.chzzkeventtodiscord.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.twitch.TwitchEventSubDtos;
import me.cocoblue.chzzkeventtodiscord.dto.twitch.TwitchSubscriptionRequestDto;
import me.cocoblue.chzzkeventtodiscord.dto.twitch.TwitchSubscriptionResponseDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.service.twitch.TwitchEventSubSignatureVerifier;
import me.cocoblue.chzzkeventtodiscord.service.twitch.TwitchSubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/twitch")
@RequiredArgsConstructor
public class TwitchSubscriptionController {
    private static final String MESSAGE_TYPE_VERIFICATION = "webhook_callback_verification";
    private static final String MESSAGE_TYPE_NOTIFICATION = "notification";

    private final TwitchSubscriptionService twitchSubscriptionService;
    private final TwitchEventSubSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;

    @PostMapping("/subscriptions")
    public ResponseEntity<TwitchSubscriptionResponseDto> create(
        @RequestBody TwitchSubscriptionRequestDto request,
        Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            TwitchSubscriptionResponseDto.fromEntity(twitchSubscriptionService.create(request, extractPrincipal(authentication)))
        );
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<TwitchSubscriptionResponseDto>> list(Authentication authentication) {
        return ResponseEntity.ok(twitchSubscriptionService.list(extractPrincipal(authentication)).stream()
            .map(TwitchSubscriptionResponseDto::fromEntity)
            .toList());
    }

    @DeleteMapping("/subscriptions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        twitchSubscriptionService.delete(id, extractPrincipal(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/eventsub", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleEventSub(
        @RequestHeader("Twitch-Eventsub-Message-Id") String messageId,
        @RequestHeader("Twitch-Eventsub-Message-Timestamp") String timestamp,
        @RequestHeader("Twitch-Eventsub-Message-Signature") String signature,
        @RequestHeader("Twitch-Eventsub-Message-Type") String messageType,
        @RequestBody String body
    ) throws Exception {
        if (!signatureVerifier.isValid(messageId, timestamp, body, signature)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid Twitch EventSub signature");
        }

        final TwitchEventSubDtos.EventSubEnvelope envelope = objectMapper.readValue(body, TwitchEventSubDtos.EventSubEnvelope.class);
        if (MESSAGE_TYPE_VERIFICATION.equals(messageType)) {
            return ResponseEntity.ok(envelope.challenge());
        }
        if (MESSAGE_TYPE_NOTIFICATION.equals(messageType) && envelope.subscription() != null && envelope.event() != null) {
            twitchSubscriptionService.notifyDiscord(envelope.subscription().id(), envelope.subscription().type(), envelope.event());
        }
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
