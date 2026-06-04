package me.cocoblue.chzzkeventtodiscord.dto.system;

import me.cocoblue.chzzkeventtodiscord.service.S3StorageAvailabilitySnapshot;

import java.time.Instant;

public record UploadCapabilityDto(
    boolean enabled,
    String reason,
    Instant checkedAt
) {
    public static UploadCapabilityDto fromStorageSnapshot(S3StorageAvailabilitySnapshot snapshot) {
        return new UploadCapabilityDto(
            snapshot.available(),
            snapshot.reason(),
            snapshot.checkedAt()
        );
    }
}
