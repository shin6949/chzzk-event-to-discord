package me.cocoblue.chzzkeventtodiscord.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkCategoryDTO;
import me.cocoblue.chzzkeventtodiscord.service.chzzk.ChzzkCategoryService;
import me.cocoblue.chzzkeventtodiscord.vo.api.ChzzkCategoryAPIResponseVO;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChzzkCategoryServiceTests {
    @Autowired
    private ChzzkCategoryService chzzkCategoryService;

    private boolean ObjectMapperTestsSucceeded = false;

    @Test
    void ObjectMapperTests() throws JsonProcessingException {
        final String testCategoryResult = "{\n" +
                "    \"code\": 200,\n" +
                "    \"message\": null,\n" +
                "    \"content\": {\n" +
                "        \"categoryType\": \"GAME\",\n" +
                "        \"categoryId\": \"Splatoon3\",\n" +
                "        \"categoryValue\": \"스플래툰 3\",\n" +
                "        \"posterImageUrl\": \"https://nng-phinf.pstatic.net/MjAyMzEyMTJfMjI1/MDAxNzAyMzY0NDUwMzk3.EjW-bu7O6TENO5F-0JOPOyoXfcQ1QJTVpCvCkGodtiQg.St5IQ_KiAIvH6K_D_bZBBpTy1npgx04bUDooDJ5IID0g.JPEG/247.%EC%8A%A4%ED%94%8C%EB%9E%98%ED%88%B0_3_%EC%99%84%EC%84%B1%EB%B3%B8_%EA%B0%95%EB%AF%B8%EC%95%A0.jpg\",\n" +
                "        \"openLiveCount\": 3,\n" +
                "        \"concurrentUserCount\": 17,\n" +
                "        \"tags\": [\n" +
                "            \"스플래툰 3\",\n" +
                "            \"TPS\",\n" +
                "            \"콘솔\",\n" +
                "            \"액션\",\n" +
                "            \"슈팅\",\n" +
                "            \"닌텐도\",\n" +
                "            \"멀티플레이\",\n" +
                "            \"스플래툰\"\n" +
                "        ],\n" +
                "        \"existLounge\": true\n" +
                "    }\n" +
                "}";
        ObjectMapper objectMapper = new ObjectMapper();
        final ChzzkCategoryAPIResponseVO apiResponseVO = objectMapper.readValue(testCategoryResult, ChzzkCategoryAPIResponseVO.class);
        final ChzzkCategoryDTO chzzkCategoryDTO = apiResponseVO.toDTO();

        log.info("DTO Result: {}", chzzkCategoryDTO);
        assertEquals(apiResponseVO.getCode(), 200);
        assertNull(apiResponseVO.getMessage());
        assertNotNull(chzzkCategoryDTO.getCategoryId());
        assertNotNull(chzzkCategoryDTO.getCategoryType());
        assertNotNull(chzzkCategoryDTO.getCategoryValue());
        assertNotNull(chzzkCategoryDTO.getPosterImageUrl());
        ObjectMapperTestsSucceeded = true;
    }

    @Test
    void getChzzkCategoryByCategoryIdAtAPI() {
        Assumptions.assumeTrue(ObjectMapperTestsSucceeded);
        final String categoryType = "GAME";
        final String categoryId = "Splatoon3";
        final ChzzkCategoryDTO chzzkCategoryDTO = chzzkCategoryService.getCategoryInfo(categoryType, categoryId);

        log.info("DTO Result: {}", chzzkCategoryDTO);
        assertNotNull(chzzkCategoryDTO.getCategoryId());
        assertNotNull(chzzkCategoryDTO.getCategoryType());
        assertNotNull(chzzkCategoryDTO.getCategoryValue());
        assertNotNull(chzzkCategoryDTO.getPosterImageUrl());
    }
}
