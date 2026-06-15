package me.cocoblue.chzzkeventtodiscord.service.twitch;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.TwitchProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class TwitchEventSubSignatureVerifier {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SIGNATURE_PREFIX = "sha256=";

    private final TwitchProperties twitchProperties;

    public boolean isValid(String messageId, String timestamp, String body, String signature) {
        if (!StringUtils.hasText(twitchProperties.eventsubSecret())
            || !StringUtils.hasText(messageId)
            || !StringUtils.hasText(timestamp)
            || !StringUtils.hasText(signature)) {
            return false;
        }
        final String expected = SIGNATURE_PREFIX + hmacSha256(messageId + timestamp + body);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256(String message) {
        try {
            final Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(twitchProperties.eventsubSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to verify Twitch EventSub signature", ex);
        }
    }
}
