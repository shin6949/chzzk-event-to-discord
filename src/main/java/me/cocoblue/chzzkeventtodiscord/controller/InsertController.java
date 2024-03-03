package me.cocoblue.chzzkeventtodiscord.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertRequestDTO;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertResponseDTO;
import me.cocoblue.chzzkeventtodiscord.service.FormInsertService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/insert")
@RequiredArgsConstructor
public class InsertController {
    private final FormInsertService formInsertService;

    @PostMapping("/form")
    public FormInsertResponseDTO insertForm(@RequestBody FormInsertRequestDTO formInsertRequestDTO) {
        log.info("Form inserted");
        return formInsertService.insertForm(formInsertRequestDTO);
    }
}
