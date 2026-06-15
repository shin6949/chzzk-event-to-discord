package me.cocoblue.chzzkeventtodiscord.service.twitch;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.TwitchProperties;
import me.cocoblue.chzzkeventtodiscord.dto.twitch.TwitchEventSubDtos;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TwitchApiClient {
    private final TwitchProperties twitchProperties;
    private final WebClient.Builder webClientBuilder;

    public TwitchEventSubDtos.TwitchUser resolveBroadcaster(String broadcasterLogin, String broadcasterUserId) {
        final String token = getAppAccessToken();
        final WebClient client = webClientBuilder.baseUrl(twitchProperties.normalizedApiBaseUrl()).build();
        final TwitchEventSubDtos.UsersResponse response = client.get()
            .uri(uriBuilder -> {
                final var builder = uriBuilder.path("/helix/users");
                if (StringUtils.hasText(broadcasterUserId)) {
                    builder.queryParam("id", broadcasterUserId.trim());
                } else {
                    builder.queryParam("login", broadcasterLogin.trim());
                }
                return builder.build();
            })
            .header("Client-Id", twitchProperties.clientId())
            .headers(headers -> headers.setBearerAuth(token))
            .retrieve()
            .bodyToMono(TwitchEventSubDtos.UsersResponse.class)
            .block();

        final List<TwitchEventSubDtos.TwitchUser> users = response == null ? List.of() : response.data();
        if (users == null || users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Twitch broadcaster not found");
        }
        return users.get(0);
    }

    public String createStreamSubscription(String type, String broadcasterUserId) {
        final String token = getAppAccessToken();
        final WebClient client = webClientBuilder.baseUrl(twitchProperties.normalizedApiBaseUrl()).build();
        final Map<String, Object> body = Map.of(
            "type", type,
            "version", "1",
            "condition", Map.of("broadcaster_user_id", broadcasterUserId),
            "transport", Map.of(
                "method", "webhook",
                "callback", twitchProperties.callbackUrl(),
                "secret", twitchProperties.eventsubSecret()
            )
        );

        final TwitchEventSubDtos.CreateSubscriptionResponse response = client.post()
            .uri("/helix/eventsub/subscriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Client-Id", twitchProperties.clientId())
            .headers(headers -> headers.setBearerAuth(token))
            .bodyValue(body)
            .retrieve()
            .bodyToMono(TwitchEventSubDtos.CreateSubscriptionResponse.class)
            .block();
        final List<TwitchEventSubDtos.EventSubSubscription> data = response == null ? List.of() : response.data();
        return data == null || data.isEmpty() ? null : data.get(0).id();
    }

    public void deleteEventSubSubscription(String subscriptionId) {
        if (!StringUtils.hasText(subscriptionId)) {
            return;
        }
        final String token = getAppAccessToken();
        webClientBuilder.baseUrl(twitchProperties.normalizedApiBaseUrl())
            .build()
            .delete()
            .uri(uriBuilder -> uriBuilder
                .path("/helix/eventsub/subscriptions")
                .queryParam("id", subscriptionId.trim())
                .build())
            .header("Client-Id", twitchProperties.clientId())
            .headers(headers -> headers.setBearerAuth(token))
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful() || response.statusCode().value() == HttpStatus.NOT_FOUND.value()) {
                    return response.releaseBody();
                }
                return response.createException().flatMap(Mono::error);
            })
            .block();
    }

    private String getAppAccessToken() {
        if (!twitchProperties.isRegistrationConfigured()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Twitch EventSub registration is not configured");
        }

        final TwitchEventSubDtos.AppTokenResponse response = webClientBuilder
            .baseUrl(twitchProperties.normalizedAuthBaseUrl())
            .build()
            .post()
            .uri("/oauth2/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("client_id", twitchProperties.clientId())
                .with("client_secret", twitchProperties.clientSecret())
                .with("grant_type", "client_credentials"))
            .retrieve()
            .bodyToMono(TwitchEventSubDtos.AppTokenResponse.class)
            .block();

        if (response == null || !StringUtils.hasText(response.accessToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to get Twitch app access token");
        }
        return response.accessToken();
    }
}
