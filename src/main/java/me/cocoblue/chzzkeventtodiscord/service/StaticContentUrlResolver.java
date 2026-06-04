package me.cocoblue.chzzkeventtodiscord.service;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.AppStaticContentProperties;
import me.cocoblue.chzzkeventtodiscord.config.S3StorageProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StaticContentUrlResolver {
    private final AppStaticContentProperties appStaticContentProperties;
    private final S3StorageProperties s3StorageProperties;

    public String resolve(String objectKeyOrUrl) {
        if (!StringUtils.hasText(objectKeyOrUrl)) {
            return objectKeyOrUrl;
        }
        if (objectKeyOrUrl.startsWith("http://") || objectKeyOrUrl.startsWith("https://")) {
            return objectKeyOrUrl;
        }

        return trimTrailingSlash(resolveBaseUrl()) + "/" + trimLeadingSlash(objectKeyOrUrl);
    }

    public boolean isManagedObjectKey(String objectKeyOrUrl) {
        return StringUtils.hasText(objectKeyOrUrl)
            && !objectKeyOrUrl.startsWith("http://")
            && !objectKeyOrUrl.startsWith("https://");
    }

    private String resolveBaseUrl() {
        if (StringUtils.hasText(appStaticContentProperties.getUrlPrefix())) {
            return appStaticContentProperties.getUrlPrefix();
        }
        return trimTrailingSlash(s3StorageProperties.getEndpoint()) + "/" + s3StorageProperties.getBucket();
    }

    private String trimTrailingSlash(String value) {
        String next = value == null ? "" : value.trim();
        while (next.endsWith("/")) {
            next = next.substring(0, next.length() - 1);
        }
        return next;
    }

    private String trimLeadingSlash(String value) {
        String next = value.trim();
        while (next.startsWith("/")) {
            next = next.substring(1);
        }
        return next;
    }
}
