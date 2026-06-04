package me.cocoblue.chzzkeventtodiscord.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey appJwtSecretKey(AppJwtProperties properties) {
        final byte[] secretBytes = resolveSecretBytes(properties.getSecret());
        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder appJwtEncoder(SecretKey appJwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(appJwtSecretKey));
    }

    @Bean
    public JwtDecoder appJwtDecoder(SecretKey appJwtSecretKey, AppJwtProperties properties) {
        final NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(appJwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getIssuer()));
        return decoder;
    }

    private byte[] resolveSecretBytes(String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("app.auth.jwt.secret must not be empty");
        }

        final byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) {
            return bytes;
        }

        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is unavailable", e);
        }
    }
}
