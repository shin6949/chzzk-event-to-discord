package me.cocoblue.chzzkeventtodiscord.data.chzzk;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    @JsonCreator
    public static ChzzkLiveStatusType forValue(String value) {
        for (ChzzkLiveStatusType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return UNKNOWN; // 알 수 없는 값 처리
    }
}
