package roomescape.payment.toss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
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
        // 타임아웃은 연결을 실제로 만드는 요청 팩토리에 건다(고수준 builder엔 해당 설정이 없다).
        // Apache HttpComponents를 쓴다: simple(HttpURLConnection)은 401 응답 바디를 인증 처리 중
        // 삼켜 토스 에러코드 매핑이 깨지고, jdk는 응답 바디 지연을 read timeout으로 못 잡는다.
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(properties.connectTimeout())
                .withReadTimeout(properties.readTimeout());
        ClientHttpRequestFactory factory = ClientHttpRequestFactoryBuilder
                .httpComponents()
                .build(settings);
        return builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(factory)
                .build();
    }
}
