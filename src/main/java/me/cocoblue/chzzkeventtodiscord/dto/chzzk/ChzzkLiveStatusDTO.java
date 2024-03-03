package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import lombok.*;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkLiveStatusType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkLiveStatusDTO {
    private String liveTitle;
    private ChzzkLiveStatusType status;
    private Long concurrentUserCount;
    private Long accumulateCount;
    private boolean paidPromotion;
    private boolean adult;
    private String chatChannelId;
    private boolean chatActive;
    private ChzzkChatAvailableGroupType chatAvailableGroup;
    private ChzzkChatAvailableConditionType chatAvailableCondition;
    private Long minFollowerMinute;
    private String categoryType;
    private String categoryId;
    private String categoryValue;
}
