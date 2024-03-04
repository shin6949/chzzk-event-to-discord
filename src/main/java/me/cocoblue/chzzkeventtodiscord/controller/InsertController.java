package me.cocoblue.chzzkeventtodiscord.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertRequestDTO;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertResponseDTO;
import me.cocoblue.chzzkeventtodiscord.service.FormInsertService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;

@Log4j2
@RestController
@RequestMapping("/insert")
@RequiredArgsConstructor
public class InsertController {
    private final FormInsertService formInsertService;

    @Value("${app.insert-password:null}")
    private String insertPassword;

    @PostMapping("/form")
    public ResponseEntity<FormInsertResponseDTO> insertForm(@RequestHeader(value = "Authorization") String password, @RequestBody FormInsertRequestDTO formInsertRequestDTO) {
        log.info("Form Insert Request Received.");
        if(!password.equals("Bearer " + insertPassword)) {
            log.warn("Invalid Authorization Token Received. Do not process more.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        FormInsertResponseDTO formInsertResponseDTO = formInsertService.insertForm(formInsertRequestDTO);
        return new ResponseEntity<>(formInsertResponseDTO, HttpStatus.ACCEPTED);
    }
}
