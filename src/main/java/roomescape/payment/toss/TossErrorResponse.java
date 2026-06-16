package roomescape.payment.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 토스 에러 응답 바디({code, message}). package-private — 어댑터 안에서 도메인 예외로 번역되고 사라진다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record TossErrorResponse(String code, String message) {
}
