package me.cocoblue.chzzkeventtodiscord.service;

import java.time.Instant;

public record S3StorageAvailabilitySnapshot(
    boolean available,
    Instant checkedAt,
    String reason,
    String endpoint,
    String bucket
) {
}
