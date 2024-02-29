package me.cocoblue.chzzkeventtodiscord.data.chzzk;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
    치지직 채팅 이용 대상 타입
 */
@Getter
@AllArgsConstructor
public enum ChzzkChatAvailableGroupType {
    FOLLOWER("FOLLOWER"),
    MANAGER("MANAGER"),
    ALL("ALL"),
    // API 변동에 대비한 기본 값
    UNKNOWN("UNKNOWN");

    private final String value;

    @JsonCreator
    public static ChzzkChatAvailableGroupType forValue(String value) {
        for (ChzzkChatAvailableGroupType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
