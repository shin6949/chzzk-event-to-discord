package me.cocoblue.chzzkeventtodiscord.service.youtube;

public record YouTubeChannelState(YouTubeChannelSnapshot channel, YouTubeVideoSnapshot liveVideo, YouTubeVideoSnapshot latestVideo) {
    public boolean live() {
        return liveVideo != null;
    }
}
