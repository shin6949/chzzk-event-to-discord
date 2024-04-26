package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableConditionType;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkChatAvailableGroupType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkLiveDetailDto {
    protected String liveId;
    protected String liveTitle;
    protected String liveImageUrl;
    protected String defaultThumbnailImageUrl;
    protected int concurrentUserCount;
    protected int accumulateCount;
    protected LocalDateTime openDate;
    private LocalDateTime closeDate;
    protected String chatChannelId;
    protected String categoryType;
    protected String categoryId;
    protected String categoryValue;
    private boolean adult;
    private boolean chatActive;
    private ChzzkChatAvailableGroupType chatAvailableGroup;
    private ChzzkChatAvailableConditionType chatAvailableCondition;
    private int minFollowerMinute;
}
