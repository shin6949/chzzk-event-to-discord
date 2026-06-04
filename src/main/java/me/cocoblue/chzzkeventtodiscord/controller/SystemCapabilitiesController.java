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

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemCapabilitiesController {
    private final S3StorageAvailabilityService s3StorageAvailabilityService;

    @GetMapping("/capabilities")
    public ResponseEntity<SystemCapabilitiesResponseDto> getCapabilities() {
        return ResponseEntity.ok(new SystemCapabilitiesResponseDto(
            new UploadCapabilitiesDto(
                UploadCapabilityDto.fromStorageSnapshot(s3StorageAvailabilityService.current())
            )
        ));
    }
}
