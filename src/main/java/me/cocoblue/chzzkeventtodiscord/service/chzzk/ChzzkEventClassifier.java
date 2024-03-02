package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDTO;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ChzzkEventClassifier {
    public boolean isOnNewLive(final ChzzkChannelDTO channelDataFromDatabase,
                            final ChzzkChannelDTO channelDataFromApi) {
        if(!channelDataFromDatabase.isOpenLive() && channelDataFromApi.isOpenLive()) {
            log.info("New live streaming started. channelId: {}", channelDataFromDatabase.getChannelId());
            return true;
        }

        log.debug("Live status is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
        return false;
    }

    public boolean isOnNewOffline(final ChzzkChannelDTO channelDataFromDatabase,
                               final ChzzkChannelDTO channelDataFromApi) {
        if(channelDataFromDatabase.isOpenLive() && !channelDataFromApi.isOpenLive()) {
            log.info("Streaming is now offline. channelId: {}", channelDataFromDatabase.getChannelId());
            return true;
        }

        log.debug("Live status is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
        return false;

    }

    public boolean isChannelInformationChanged(final ChzzkChannelDTO channelDataFromDatabase,
                                               final ChzzkChannelDTO channelDataFromApi) {
        if(!channelDataFromDatabase.equals(channelDataFromApi)) {
            log.info("Channel information is changed. channelId: {}", channelDataFromDatabase.getChannelId());
            return true;
        }

        log.debug("Channel information is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
        return false;
    }

    public boolean isFollowerCountChanged(final ChzzkChannelDTO channelDataFromDatabase,
                                          final ChzzkChannelDTO channelDataFromApi) {
        if(channelDataFromDatabase.getFollowerCount() != channelDataFromApi.getFollowerCount()) {
            log.info("Follower count is changed. channelId: {}", channelDataFromDatabase.getChannelId());
            return true;
        }

        log.debug("Follower count is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
        return false;
    }
}
