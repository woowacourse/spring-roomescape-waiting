package roomescape.payment.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 토스 연동 설정. 시크릿 키는 코드에 하드코딩하지 않고 application.yml/환경변수로 외부화한다.
 * clientKey는 공개(프론트 위젯)지만, secretKey는 서버 승인 전용 비밀이다.
 */
@ConfigurationProperties(prefix = "toss")
public record TossProperties(String clientKey, String secretKey, String baseUrl, String confirmUrl) {
}
