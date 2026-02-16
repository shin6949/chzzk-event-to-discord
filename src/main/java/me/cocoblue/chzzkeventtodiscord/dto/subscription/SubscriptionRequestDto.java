package me.cocoblue.chzzkeventtodiscord.dto.subscription;

import lombok.Data;
import me.cocoblue.chzzkeventtodiscord.data.LanguageIsoData;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;

@Data
public class SubscriptionRequestDto {
    private String channelId;
    private String formOwnerChannelId;
    private ChzzkSubscriptionType subscriptionType;
    private Long webhookId;
    private Long botProfileId;
    private LanguageIsoData language;
    private Integer intervalMinute;
    private Boolean enabled;
    private String content;
    private String colorHex;
    private Boolean showDetail;
    private Boolean showThumbnail;
    private Boolean showViewerCount;
    private Boolean showTag;
}
