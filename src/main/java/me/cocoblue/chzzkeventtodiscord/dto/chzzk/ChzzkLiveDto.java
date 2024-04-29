package me.cocoblue.chzzkeventtodiscord.dto.chzzk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkLiveDto {
    private String liveTitle;
    private String liveImageUrl;
    private String defaultThumbnailImageUrl;
    private int concurrentUserCount;
    private int accumulateCount;
    private LocalDateTime openDate;
    private String liveId;
    private String chatChannelId;
    private String categoryType;
    private String liveCategory;
    private String liveCategoryValue;
    private String channelId;
}
