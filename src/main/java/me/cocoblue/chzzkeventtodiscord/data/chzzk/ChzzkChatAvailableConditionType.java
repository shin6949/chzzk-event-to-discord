package me.cocoblue.chzzkeventtodiscord.data.chzzk;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 치지직 채팅 이용 조건
 */
@Getter
@AllArgsConstructor
public enum ChzzkChatAvailableConditionType {
    NONE("NONE", "chat.condition.none"),
    REAL_NAME("REAL_NAME", "chat.condition.real-name"),
    // API 변동에 대비한 기본 값
    UNKNOWN("UNKNOWN", "chat.condition.unknown");

    private final String value;
    private final String stringKey;

    public static ChzzkChatAvailableConditionType forValue(String value) {
        for (ChzzkChatAvailableConditionType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return UNKNOWN; // 알 수 없는 값 처리
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
