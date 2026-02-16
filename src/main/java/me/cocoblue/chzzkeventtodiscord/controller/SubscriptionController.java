package me.cocoblue.chzzkeventtodiscord.controller;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.subscription.SubscriptionRequestDto;
import me.cocoblue.chzzkeventtodiscord.dto.subscription.SubscriptionResponseDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.service.SubscriptionCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionCrudService subscriptionCrudService;

    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> create(
        @RequestBody SubscriptionRequestDto request,
        Authentication authentication
    ) {
        final ChzzkPrincipal principal = extractPrincipal(authentication);
        final SubscriptionResponseDto response = SubscriptionResponseDto.fromEntity(
            subscriptionCrudService.create(request, principal)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SubscriptionResponseDto>> list(Pageable pageable, Authentication authentication) {
        final ChzzkPrincipal principal = extractPrincipal(authentication);
        final Page<SubscriptionResponseDto> response = subscriptionCrudService.list(principal, pageable)
            .map(SubscriptionResponseDto::fromEntity);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponseDto> get(
        @PathVariable Long subscriptionId,
        Authentication authentication
    ) {
        final ChzzkPrincipal principal = extractPrincipal(authentication);
        final SubscriptionResponseDto response = SubscriptionResponseDto.fromEntity(
            subscriptionCrudService.get(subscriptionId, principal)
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponseDto> update(
        @PathVariable Long subscriptionId,
        @RequestBody SubscriptionRequestDto request,
        Authentication authentication
    ) {
        final ChzzkPrincipal principal = extractPrincipal(authentication);
        final SubscriptionResponseDto response = SubscriptionResponseDto.fromEntity(
            subscriptionCrudService.update(subscriptionId, request, principal)
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long subscriptionId,
        Authentication authentication
    ) {
        final ChzzkPrincipal principal = extractPrincipal(authentication);
        subscriptionCrudService.delete(subscriptionId, principal);
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
