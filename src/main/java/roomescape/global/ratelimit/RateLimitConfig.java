package roomescape.global.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rate-limit 모듈의 설정.
 * 인·아웃 한도 설정(rate-limit.* / outbound-rate-limit.*) 등록과, 토큰 버킷이 사용할 시계({@link NanoClock}) 빈을 제공한다.
 */
@Configuration
@EnableConfigurationProperties({InboundRateLimitProperties.class, OutboundRateLimitProperties.class})
public class RateLimitConfig {

    @Bean
    public NanoClock nanoClock() {
        return System::nanoTime;
    }
}
