package me.cocoblue.chzzkeventtodiscord.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChzzkCategoryType {
    GAME("GAME"),
    ETC("ETC");

    private final String value;
}
