package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ChzzkEventReader {
    public void readEvent() {
        log.info("Read event from Chzzk API.");
    }
}
