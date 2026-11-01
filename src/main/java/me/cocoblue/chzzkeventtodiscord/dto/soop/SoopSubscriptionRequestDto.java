package me.cocoblue.chzzkeventtodiscord.dto.soop;

import lombok.Data;

@Data
public class SoopSubscriptionRequestDto {
    private String soopUserId;
    private String soopChannelName;
    private Long webhookId;
    private Long botProfileId;
    private Boolean enabled;
    private Boolean notifyOnline;
    private Boolean notifyOffline;
    private String content;
    private String colorHex;
}
