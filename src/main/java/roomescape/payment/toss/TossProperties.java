package roomescape.payment.toss;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 토스 연동 설정. 시크릿 키는 코드에 하드코딩하지 않고 application.yml/환경변수로 외부화한다.
 * clientKey는 공개(프론트 위젯)지만, secretKey는 서버 승인 전용 비밀이다.
 * connectTimeout/readTimeout: 느린 토스 호출이 우리 스레드를 무한정 붙잡지 못하게 하는 방어값(외부화).
 */
@ConfigurationProperties(prefix = "toss")
public record TossProperties(String clientKey, String secretKey, String baseUrl, String confirmUrl,
                             Duration connectTimeout, Duration readTimeout) {
}
