package me.cocoblue.chzzkeventtodiscord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FormInsertResponseDto {
    @JsonProperty("isSuccess")
    private Boolean isSuccess;
    @JsonProperty("registeredFormId")
    private Long registeredFormId;
    @JsonProperty("registeredWebhookId")
    private Long registeredWebhookId;
    @JsonProperty("registeredBotProfileId")
    private Long registeredBotProfileId;
}
