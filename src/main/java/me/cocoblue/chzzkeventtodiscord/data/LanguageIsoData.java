package me.cocoblue.chzzkeventtodiscord.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final String code;
    private final String koreanName;

    private static final Map<String, LanguageIsoData> descriptions = Collections.unmodifiableMap(Stream.of(values()).collect(Collectors.toMap(LanguageIsoData::getCode, Function.identity())));

    public static LanguageIsoData find(String code) {
        return Optional.ofNullable(descriptions.get(code)).orElse(Other);
    }
}
