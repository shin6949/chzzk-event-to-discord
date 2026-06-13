package me.cocoblue.chzzkeventtodiscord.dto.twitch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public final class TwitchEventSubDtos {
    private TwitchEventSubDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UsersResponse(List<TwitchUser> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TwitchUser(
        String id,
        String login,
        @JsonProperty("display_name") String displayName
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateSubscriptionResponse(List<EventSubSubscription> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventSubSubscription(String id, String type, String status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventSubEnvelope(
        String challenge,
        EventSubSubscription subscription,
        Map<String, Object> event
    ) {
    }
}
