package roomescape.payment.infrastructure;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TossProperties.class)
public class TossPaymentConfig {

    @Bean
    public RestClient tossRestClient(TossProperties properties,
                                     OutboundRateLimitInterceptor outboundRateLimitInterceptor,
                                     RetryAfterInterceptor retryAfterInterceptor) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(timeoutRequestFactory(properties))
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth(properties.secretKey()))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // 나가는 한도(호출당 토큰 1개)를 먼저 적용한 뒤, 토스 429에 대한 백오프 재시도를 건다.
                .requestInterceptor(outboundRateLimitInterceptor)
                .requestInterceptor(retryAfterInterceptor)
                .build();
    }

    /**
     * 느린 토스 응답이 우리 스레드를 무한정 붙잡지 못하도록 connect/read 타임아웃을 건다.
     * jdk 팩토리는 응답 바디 지연을 read timeout으로 못 잡으므로 simple 팩토리를 쓴다.
     */
    private ClientHttpRequestFactory timeoutRequestFactory(TossProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeout());
        factory.setReadTimeout(properties.readTimeout());
        return factory;
    }

    /**
     * 시크릿 키 뒤에 콜론을 붙여(비밀번호 없음) UTF-8로 인코딩한 Basic 인증 헤더를 만든다.
     */
    private String basicAuth(String secretKey) {
        String encoded = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
