package me.cocoblue.chzzkeventtodiscord.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChzzkSubscriptionType {
    STREAM_ONLINE,
    STREAM_OFFLINE,
    CHANNEL_UPDATE
}
