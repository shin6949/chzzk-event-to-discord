package me.cocoblue.chzzkeventtodiscord.controller;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.PageResponseDto;
import me.cocoblue.chzzkeventtodiscord.dto.youtube.YouTubeDtos;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.service.youtube.YouTubeSubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/youtube/subscriptions")
@RequiredArgsConstructor
public class YouTubeSubscriptionController {
    private final YouTubeSubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<YouTubeDtos.SubscriptionResponse> create(
        @RequestBody YouTubeDtos.SubscriptionRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            YouTubeDtos.SubscriptionResponse.fromEntity(subscriptionService.create(request, extractPrincipal(authentication)))
        );
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<YouTubeDtos.SubscriptionResponse>> list(Pageable pageable, Authentication authentication) {
        Page<YouTubeDtos.SubscriptionResponse> page = subscriptionService.list(extractPrincipal(authentication), pageable)
            .map(YouTubeDtos.SubscriptionResponse::fromEntity);
        return ResponseEntity.ok(PageResponseDto.from(page));
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<YouTubeDtos.SubscriptionResponse> get(@PathVariable Long subscriptionId, Authentication authentication) {
        return ResponseEntity.ok(
            YouTubeDtos.SubscriptionResponse.fromEntity(subscriptionService.get(subscriptionId, extractPrincipal(authentication)))
        );
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> delete(@PathVariable Long subscriptionId, Authentication authentication) {
        subscriptionService.delete(subscriptionId, extractPrincipal(authentication));
        return ResponseEntity.noContent().build();
    }

    private ChzzkPrincipal extractPrincipal(Authentication authentication) {
        Object principalObject = authentication.getPrincipal();
        if (principalObject instanceof ChzzkPrincipal chzzkPrincipal) {
            return chzzkPrincipal;
        }
        AppRole role = authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())) ? AppRole.ADMIN : AppRole.USER;
        return new ChzzkPrincipal(authentication.getName(), role);
    }
}
