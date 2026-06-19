package roomescape.feature.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 토스 결제 RestClient의 '전송(transport)' 설정만 담당한다.
 *
 * 타임아웃(requestFactory)을 빌더에 적용해 두며, baseUrl·인증은
 * 이 빌더를 주입받는 {@link roomescape.feature.payment.TossPaymentClient} 가 얹는다.
 * requestFactory 적용을 빌더에 두어 클라이언트가 직접 호출하지 않게 한 것은
 * {@code MockRestServiceServer}(요청 팩토리를 바꿔치기하는 방식) 테스트 seam을 보존하기 위함이다.
 */
@Configuration
@EnableConfigurationProperties(TossPaymentProperties.class)
public class PaymentRestClientConfig {

    @Bean
    public RestClient.Builder tossRestClientBuilder(TossPaymentProperties properties) {
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory(properties));
    }

    private ClientHttpRequestFactory clientHttpRequestFactory(TossPaymentProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeout());
        factory.setReadTimeout(properties.readTimeout());

        return factory;
    }
}
