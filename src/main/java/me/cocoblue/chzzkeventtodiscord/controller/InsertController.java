package me.cocoblue.chzzkeventtodiscord.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertRequestDto;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertResponseDto;
import me.cocoblue.chzzkeventtodiscord.service.FormInsertService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/form")
@RequiredArgsConstructor
public class InsertController {
    private final FormInsertService formInsertService;

    @Value("${app.insert-password:null}")
    private String insertPassword;

    @PostMapping(value = {"/insert", "/insert/"})
    public ResponseEntity<FormInsertResponseDto> insertForm(@RequestHeader(value = "Authorization") String password,
                                                            @RequestBody FormInsertRequestDto formInsertRequestDto) {
        log.info("Form Insert Request Received.");
        if (!password.equals("Bearer " + insertPassword)) {
            log.warn("Invalid Authorization Token Received. Do not process more.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        FormInsertResponseDto formInsertResponseDTO = formInsertService.insertForm(formInsertRequestDto);
        return new ResponseEntity<>(formInsertResponseDTO, HttpStatus.OK);
    }
}
