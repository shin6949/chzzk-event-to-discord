package me.cocoblue.chzzkeventtodiscord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FormInsertRequestDto {
    @JsonProperty("channelId")
    private String channelId;
    @JsonProperty("channelName")
    private String channelName;
    @JsonProperty("content")
    private String content;
    @JsonProperty("colorHex")
    private String colorHex;
    @JsonProperty("subscriptionType")
    private ChzzkSubscriptionType subscriptionType;
    @JsonProperty("showDetail")
    private Boolean showDetail;
    @JsonProperty("webhookId")
    private Long webhookId;
    @JsonProperty("webhookName")
    private String webhookName;
    @JsonProperty("webhookUrl")
    private String webhookUrl;
    @JsonProperty("botProfileId")
    private Long botProfileId;
    @JsonProperty("botUsername")
    private String botUsername;
    @JsonProperty("botAvatarUrl")
    private String botAvatarUrl;
    @JsonProperty("ownerChannelId")
    private String ownerChannelId;
    @JsonProperty("ownerChannelName")
    private String ownerChannelName;
    @JsonProperty("intervalMinute")
    private Integer intervalMinute;
    @JsonProperty("language")
    private LanguageIsoData language;
    @JsonProperty("enabled")
    private Boolean enabled;
}
