package me.cocoblue.chzzkeventtodiscord.data.chzzk;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChzzkSubscriptionType {
    STREAM_ONLINE,
    STREAM_OFFLINE,
    CHANNEL_UPDATE,
    /* @since 1.2 */
    STREAM_ONLINE_AND_OFFLINE,
}