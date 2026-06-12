package me.cocoblue.chzzkeventtodiscord.controller;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.system.SystemCapabilitiesResponseDto;
import me.cocoblue.chzzkeventtodiscord.dto.system.UploadCapabilitiesDto;
import me.cocoblue.chzzkeventtodiscord.dto.system.UploadCapabilityDto;
import me.cocoblue.chzzkeventtodiscord.service.S3StorageAvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code SystemCapabilitiesController}는 HTTP API 요청을 받아 서비스 계층으로 위임합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemCapabilitiesController {
  private final S3StorageAvailabilityService s3StorageAvailabilityService;

  /**
   * {@code getCapabilities}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping("/capabilities")
  public ResponseEntity<SystemCapabilitiesResponseDto> getCapabilities() {
    return ResponseEntity.ok(
        new SystemCapabilitiesResponseDto(
            new UploadCapabilitiesDto(
                UploadCapabilityDto.fromStorageSnapshot(s3StorageAvailabilityService.current()))));
  }
}
