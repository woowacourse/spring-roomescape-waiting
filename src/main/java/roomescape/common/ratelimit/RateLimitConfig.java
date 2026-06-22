package roomescape.common.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 들어오는 요청 Rate Limit 조립. rate-limit.* 설정으로 토큰 버킷을 만들어 인터셉터로 노출한다.
 * 버킷은 인터셉터가 들고 있는 한 인스턴스(싱글톤)라, 보호 대상 엔드포인트 전체가 같은 한도를 공유한다.
 * (인터셉터의 경로 등록은 WebMvcConfig가 맡는다.)
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(RateLimitProperties properties) {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(
                properties.capacity(), properties.refillPerSec(), System::nanoTime);
        return new RateLimitInterceptor(rateLimiter);
    }
}
