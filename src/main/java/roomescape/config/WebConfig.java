package roomescape.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.ratelimit.RateLimitInterceptor;
import roomescape.ratelimit.TokenBucketRateLimiter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final TokenBucketRateLimiter rateLimiter;

  public WebConfig(
      @Value("${rate-limit.capacity}") long capacity,
      @Value("${rate-limit.refill-per-second}") double refillPerSecond
  ) {
    this.rateLimiter = new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new RateLimitInterceptor(rateLimiter))
        .addPathPatterns("/payments/**", "/reservations/**");
  }
}
