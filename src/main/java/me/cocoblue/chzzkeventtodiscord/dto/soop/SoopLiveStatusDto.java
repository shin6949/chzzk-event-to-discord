package me.cocoblue.chzzkeventtodiscord.dto.soop;

public record SoopLiveStatusDto(
    String userId,
    String userNick,
    String profileImageUrl,
    boolean live,
    String broadNo,
    String title,
    String thumbnailUrl,
    String startedAt,
    String viewerCount,
    String liveUrl
) {
    public static SoopLiveStatusDto offline(String userId) {
        return new SoopLiveStatusDto(userId, userId, null, false, null, null, null, null, null, "https://ch.sooplive.co.kr/" + userId);
    }
}
