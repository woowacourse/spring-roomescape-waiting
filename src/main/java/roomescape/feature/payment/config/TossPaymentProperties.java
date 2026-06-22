package roomescape.feature.payment.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 토스 결제 연동 설정값. "Toss를 어떻게 호출하는가"(엔드포인트·자격증명·전송 타임아웃)가 한곳에 응집된다.
 * 타임아웃은 이 클라이언트의 고유 정책이므로 여기서 소유하고, 값이 없으면 기본값을 쓴다.
 */
@ConfigurationProperties("toss.payments")
public record TossPaymentProperties(
        String baseUrl,
        String secretKey,
        @DefaultValue("3s") Duration connectTimeout,
        @DefaultValue("60s") Duration readTimeout
) {
}
