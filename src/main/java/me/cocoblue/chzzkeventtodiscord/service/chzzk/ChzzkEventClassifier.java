package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDTO;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Log4j2
@Service
public class ChzzkEventClassifier {
    public boolean isOnNewLive(final ChzzkChannelDTO channelDataFromDatabase,
                               final ChzzkChannelDTO channelDataFromApi) {
        if (!channelDataFromDatabase.isOpenLive() && channelDataFromApi.isOpenLive()) {
            log.info("New live streaming started. channelId: {}", channelDataFromDatabase.getChannelId());
            return true;
        }

        log.debug("Live status is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
        return false;
    }

    public boolean isOnNewOffline(final ChzzkChannelDTO channelDataFromDatabase,
                                  final ChzzkChannelDTO channelDataFromApi) {
        if (channelDataFromDatabase.isOpenLive() && !channelDataFromApi.isOpenLive()) {
            log.info("Streaming is now offline. channelId: {}", channelDataFromDatabase.getChannelId());
            return true;
        }

        log.debug("Live status is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
        return false;

    }

    public boolean isChannelInformationChanged(final ChzzkChannelDTO channelDataFromDatabase,
                                               final ChzzkChannelDTO channelDataFromApi) {
        compareChzzkChannelDTO(channelDataFromDatabase, channelDataFromApi);

        if (!channelDataFromDatabase.equals(channelDataFromApi)) {
            log.info("Channel information is changed. channelId: {}", channelDataFromDatabase.getChannelId());
            return true;
        }

        log.debug("Channel information is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
        return false;
    }

    public boolean isFollowerCountChanged(final ChzzkChannelDTO channelDataFromDatabase,
                                          final ChzzkChannelDTO channelDataFromApi) {
        if (channelDataFromDatabase.getFollowerCount() != channelDataFromApi.getFollowerCount()) {
            log.info("Follower count is changed. channelId: {}", channelDataFromDatabase.getChannelId());
            return true;
        }

        log.debug("Follower count is not changed. channelId: {}", channelDataFromDatabase.getChannelId());
        return false;
    }

    void compareChzzkChannelDTO(ChzzkChannelDTO dto1, ChzzkChannelDTO dto2) {
        StringBuilder differences = new StringBuilder();

        if (!Objects.equals(dto1.getChannelId(), dto2.getChannelId())) {
            differences.append("ChannelId, ");
        }
        if (!Objects.equals(dto1.getChannelName(), dto2.getChannelName())) {
            differences.append("ChannelName, ");
        }
        if (!Objects.equals(dto1.getChannelImageUrl(), dto2.getChannelImageUrl())) {
            differences.append("ChannelImageUrl, ");
        }
        if (!Objects.equals(dto1.getVerifiedMark(), dto2.getVerifiedMark())) {
            differences.append("VerifiedMark, ");
        }
        if (!Objects.equals(dto1.getChannelDescription(), dto2.getChannelDescription())) {
            differences.append("ChannelDescription, ");
        }
        if (dto1.getFollowerCount() != dto2.getFollowerCount()) {
            differences.append("FollowerCount, ");
        }
        if (dto1.isOpenLive() != dto2.isOpenLive()) {
            differences.append("OpenLive, ");
        }
        if (!Objects.equals(dto1.isSubscriptionAvailability(), dto2.isSubscriptionAvailability())) {
            differences.append("SubscriptionAvailability, ");
        }

        if (differences.length() > 0) {
            // Remove the trailing comma and space
            differences.setLength(differences.length() - 2);
            log.info("Different fields: " + differences);
        } else {
            log.info("Objects are equal.");
        }
    }
}
