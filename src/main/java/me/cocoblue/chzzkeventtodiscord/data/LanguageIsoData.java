package me.cocoblue.chzzkeventtodiscord.data;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code LanguageIsoData}는 애플리케이션에서 사용하는 코드 값을 정의합니다.
 *
 * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
 *
 * @since Ver.0.1
 */
@Getter
@AllArgsConstructor
public enum LanguageIsoData {
  English("en", "영어"),
  Korean("ko", "한국어"),
  Spanish("es", "스페인어"),
  German("de", "독일어"),
  Portuguese("pt", "포르투갈어"),
  Russian("ru", "러시아어"),
  French("fr", "프랑스어"),
  Japanese("ja", "일본어"),
  Chinese("zh", "중국어"),
  Italian("it", "이탈리아어"),
  Turkish("tr", "터키어"),
  Polish("pl", "폴란드어"),
  Arabic("ar", "아랍어"),
  Thai("th", "태국어"),
  Czech("cs", "체코어"),
  Hungarian("hu", "헝가리어"),
  Dutch("nl", "네덜란드어"),
  Finnish("fi", "핀란드어"),
  Swedish("sv", "스웨덴어"),
  Danish("da", "덴마크어"),
  Norwegian("no", "노르웨이어"),
  Greek("el", "그리스어"),
  Slovak("sk", "슬로바키아어"),
  Romanian("ro", "루마니아어"),
  Bulgarian("bg", "불가리아어"),
  Indonesian("id", "인도네시아어"),
  Ukrainian("uk", "우크라이나어"),
  Tagalog("tl", "타갈로그어"),
  Catalan("ca", "카탈루냐어"),
  Hindi("hi", "힌디어"),
  Malay("ms", "말레이어"),
  Vietnamese("vi", "베트남어"),
  Uzbek("uz", "우즈베키스탄어"),
  American_Sign_Language("asl", "미국 수화"),
  ZH_HK("zh-hk", "홍콩 번체"),
  Other("other", "기타");

  private static final Map<String, LanguageIsoData> descriptions =
      Collections.unmodifiableMap(
          Stream.of(values())
              .collect(Collectors.toMap(LanguageIsoData::getCode, Function.identity())));
  private final String code;
  private final String koreanName;

  /**
   * {@code find}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2024-02-27 02:04:45 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 5150c0f.
   *
   * @since Ver.0.1
   */
  public static LanguageIsoData find(String code) {
    return Optional.ofNullable(descriptions.get(code)).orElse(Other);
  }
}
