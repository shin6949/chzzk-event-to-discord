package me.cocoblue.chzzkeventtodiscord.service.youtube;

import java.time.OffsetDateTime;

public record YouTubeVideoSnapshot(
    String videoId,
    String title,
    String description,
    String thumbnailUrl,
    OffsetDateTime publishedAt
) {
}
