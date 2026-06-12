package me.cocoblue.chzzkeventtodiscord.service;

import java.time.Instant;

/**
 * {@code S3StorageAvailabilitySnapshot}는 불변 데이터 전달 구조를 정의합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
public record S3StorageAvailabilitySnapshot(
    boolean available, Instant checkedAt, String reason, String endpoint, String bucket) {}
