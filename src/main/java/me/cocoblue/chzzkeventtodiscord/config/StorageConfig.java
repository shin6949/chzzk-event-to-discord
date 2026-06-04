package me.cocoblue.chzzkeventtodiscord.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties({
    S3StorageProperties.class,
    StorageUploadProperties.class,
    AppStaticContentProperties.class
})
public class StorageConfig {
    @Bean
    public S3Client s3Client(S3StorageProperties properties) {
        final S3Configuration s3Configuration = S3Configuration.builder()
            .pathStyleAccessEnabled(properties.isForcePathStyle())
            .chunkedEncodingEnabled(!properties.isDisableChunkedEncoding())
            .build();

        return S3Client.builder()
            .region(Region.of(properties.getRegion()))
            .endpointOverride(URI.create(properties.getEndpoint()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
            ))
            .httpClientBuilder(ApacheHttpClient.builder()
                .expectContinueEnabled(false)
                .connectionTimeout(Duration.ofMillis(properties.getConnectionTimeoutMillis()))
                .socketTimeout(Duration.ofMillis(properties.getSocketTimeoutMillis())))
            .overrideConfiguration(configuration -> configuration
                .apiCallAttemptTimeout(Duration.ofMillis(properties.getApiCallAttemptTimeoutMillis()))
                .apiCallTimeout(Duration.ofMillis(properties.getApiCallTimeoutMillis())))
            .serviceConfiguration(s3Configuration)
            .build();
    }
}
