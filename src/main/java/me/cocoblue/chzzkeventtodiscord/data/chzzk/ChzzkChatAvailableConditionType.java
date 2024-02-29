package me.cocoblue.chzzkeventtodiscord.data.chzzk;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 치지직 채팅 이용 조건
 */
@Getter
@AllArgsConstructor
public enum ChzzkChatAvailableConditionType {
    NONE("NONE"),
    REAL_NAME("REAL_NAME"),
    // API 변동에 대비한 기본 값
    UNKNOWN("UNKNOWN");

    private final String value;

    @JsonCreator
    public static ChzzkChatAvailableConditionType forValue(String value) {
        for (ChzzkChatAvailableConditionType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return UNKNOWN; // 알 수 없는 값 처리
    }
}
