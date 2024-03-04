package me.cocoblue.chzzkeventtodiscord.service.chzzk;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.data.chzzk.ChzzkSubscriptionType;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkSubscriptionFormEntity;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDTO;
import me.cocoblue.chzzkeventtodiscord.service.ChzzkSubscriptionFormService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChzzkEventReader {
    private final ChzzkSubscriptionFormService subscriptionFormService;
    private final ChzzkChannelRepository chzzkChannelRepository;
    private final ChzzkChannelService chzzkChannelService;
    private final ChzzkEventSender chzzkEventSender;
    private final ChzzkEventClassifier chzzkEventClassifier;
    @Value("${app.is-test:false}")
    private boolean isTest;

    @Scheduled(fixedRateString = "#{${chzzk.check-interval:30} * 1000}")
    public void readEvent() {
        log.info("Read event from Chzzk API. time: {}", LocalDateTime.now());
        if(isTest) {
            log.info("Test mode is enabled. Skip scheduled task.");
            return;
        }

        final List<ChzzkSubscriptionFormEntity> subscriptionFormsAllEnabled = subscriptionFormService.findAllByEnabled(true);
        final Set<String> needToFetchChannelIds = subscriptionFormsAllEnabled.parallelStream()
                .map(ChzzkSubscriptionFormEntity::getChzzkChannelEntity)
                .map(ChzzkChannelEntity::getChannelId)
                .collect(Collectors.toSet());

        log.info("Need to fetch channel ids count: {}", needToFetchChannelIds.size());
        needToFetchChannelIds.forEach(this::classifyEventAndRunTrigger);

        log.info("Scheduled task finished.");
    }

    @Async
    public void classifyEventAndRunTrigger(final String channelId) {
        log.info("Classify event and send to Discord. channelId: {}", channelId);
        final ChzzkChannelDTO channelDataFromDatabase = new ChzzkChannelDTO(chzzkChannelRepository.findById(channelId).orElseThrow());
        // API 상에서 채널 정보를 가져오면 정보는 자동으로 DB에 업데이트 되므로 별도의 로직이 필요 없음. 즉, 상기 코드가 선행되어야 비교가 가능하다.
        final ChzzkChannelDTO channelDataFromApi = chzzkChannelService.getChannelByChannelIdAtAPI(channelId);

        // Database에서 가져온 데이터와 API에서 가져온 데이터를 비교하여 이벤트를 분류한다.
        // Live 이벤트와 Offline 이벤트는 공존할 수 없으므로 else if로 구분하여 불필요한 로직 실행을 막음.
        if (chzzkEventClassifier.isOnNewLive(channelDataFromDatabase, channelDataFromApi)) {
            chzzkEventSender.sendStreamEvent(channelDataFromApi, ChzzkSubscriptionType.STREAM_ONLINE);
        } else if (chzzkEventClassifier.isOnNewOffline(channelDataFromDatabase, channelDataFromApi)) {
            chzzkEventSender.sendStreamEvent(channelDataFromApi, ChzzkSubscriptionType.STREAM_OFFLINE);
        }

        // Channel 정보가 변경되었을 때, Discord로 알림을 보낸다.
        // TODO: 각 Form의 INTERVAL에 따라서 변경 사항을 감지할 수 있는 기능을 추가해야함.
        if (chzzkEventClassifier.isChannelInformationChanged(channelDataFromDatabase, channelDataFromApi)) {
            log.info("Channel information changed. channelId: {}", channelId);
            chzzkEventSender.sendChannelUpdateEvent(channelDataFromApi);
        }
    }
}
