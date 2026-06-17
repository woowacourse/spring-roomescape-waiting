package roomescape.payment.toss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/**
 * 토스 연동 설정. 토스용 RestClient를 여기서 조립한다 — base-url과 서버 승인용 Basic 인증 헤더를
 * 기본값으로 박아, 어댑터(TossPaymentGateway)는 '어느 경로에 무엇을 보낼지'만 알면 되게 한다.
 */
@Configuration
@EnableConfigurationProperties(TossProperties.class)
public class TossConfig {

    @Bean
    public RestClient tossRestClient(RestClient.Builder builder, TossProperties properties) {
        // Basic 인증: base64(시크릿키 + ":"). 콜론 뒤 비밀번호는 비우고 UTF-8로 인코딩한다.
        String basic = Base64.getEncoder()
                .encodeToString((properties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));
        return builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .build();
    }
}
