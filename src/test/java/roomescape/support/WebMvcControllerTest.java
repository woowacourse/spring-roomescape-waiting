package roomescape.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import roomescape.global.ratelimit.NanoClockConfig;
import roomescape.global.ratelimit.RateLimitConfig;
import roomescape.global.ratelimit.RateLimiters;
import roomescape.global.ratelimit.InboundRateLimitInterceptor;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest
@Import({
        InboundRateLimitInterceptor.class,
        RateLimiters.class,
        RateLimitConfig.class,
        NanoClockConfig.class
})
public @interface WebMvcControllerTest {
    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
