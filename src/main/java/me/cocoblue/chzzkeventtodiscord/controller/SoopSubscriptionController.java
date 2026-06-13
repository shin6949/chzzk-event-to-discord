package me.cocoblue.chzzkeventtodiscord.controller;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.PageResponseDto;
import me.cocoblue.chzzkeventtodiscord.dto.soop.SoopSubscriptionRequestDto;
import me.cocoblue.chzzkeventtodiscord.dto.soop.SoopSubscriptionResponseDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.service.soop.SoopSubscriptionCrudService;
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
@RequestMapping("/api/v1/soop/subscriptions")
@RequiredArgsConstructor
public class SoopSubscriptionController {
    private final SoopSubscriptionCrudService soopSubscriptionCrudService;

    @PostMapping
    public ResponseEntity<SoopSubscriptionResponseDto> create(@RequestBody SoopSubscriptionRequestDto request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            SoopSubscriptionResponseDto.fromEntity(soopSubscriptionCrudService.create(request, extractPrincipal(authentication)))
        );
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<SoopSubscriptionResponseDto>> list(Pageable pageable, Authentication authentication) {
        final Page<SoopSubscriptionResponseDto> response = soopSubscriptionCrudService.list(extractPrincipal(authentication), pageable)
            .map(SoopSubscriptionResponseDto::fromEntity);
        return ResponseEntity.ok(PageResponseDto.from(response));
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> delete(@PathVariable Long subscriptionId, Authentication authentication) {
        soopSubscriptionCrudService.delete(subscriptionId, extractPrincipal(authentication));
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
