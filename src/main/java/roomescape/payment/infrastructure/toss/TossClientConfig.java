package roomescape.payment.infrastructure.toss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TossClientConfig {

    @Bean("tossRestClient")
    public RestClient tossRestClient(
            @Value("${payment.toss.base-url}") String baseUrl,
            @Value("${payment.toss.secret-key}") String secretKey,
            @Value("${payment.toss.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${payment.toss.read-timeout-ms}") int readTimeoutMs
    ) {
        String credential = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        var requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                .setResponseTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                .build();

        var httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        var factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credential)
                .requestFactory(factory)
                .build();
    }
}
