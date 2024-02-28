package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkLiveStatusDTO {
    private String liveTitle;
    private String status;
    private Long concurrentUserCount;
    private Long accumulateCount;
    private boolean paidPromotion;
    private boolean adult;
    private String chatChannelId;
    private String livePollingStatusJson;
    private String faultStatus;
    private String userAdultStatus;
    private boolean chatActive;
    private String chatAvailableGroup;
    private String chatAvailableCondition;
    private Long minFollowerMinute;
}
