package roomescape.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Toss API 에러 응답 본문. HTTP 상태코드와 함께 {code, message} 형태로 내려온다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TossErrorResponse(String code, String message) {

}

