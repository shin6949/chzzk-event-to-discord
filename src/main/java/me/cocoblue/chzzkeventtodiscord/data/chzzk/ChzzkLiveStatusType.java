package me.cocoblue.chzzkeventtodiscord.data.chzzk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChzzkLiveStatusType {
    CLOSE("CLOSE"),
    OPEN("OPEN"),
    // API 변동에 대비한 기본 값
    UNKNOWN("UNKNOWN");

    private final String value;

    public static ChzzkLiveStatusType forValue(String value) {
        for (ChzzkLiveStatusType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return UNKNOWN; // 알 수 없는 값 처리
    }

    @JsonValue // 이 어노테이션을 추가하여 직렬화할 때 사용할 값을 지정합니다.
    public String getValue() {
        return value;
    }
}
