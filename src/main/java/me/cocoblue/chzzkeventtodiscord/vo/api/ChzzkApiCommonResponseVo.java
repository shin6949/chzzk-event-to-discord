package me.cocoblue.chzzkeventtodiscord.vo.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkApiCommonResponseVo {
    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;
}