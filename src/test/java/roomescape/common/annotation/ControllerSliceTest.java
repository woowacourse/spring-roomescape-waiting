package roomescape.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import roomescape.common.TestArgumentResolverConfig;
import roomescape.common.auth.config.AuthInterceptorConfig;
import roomescape.common.auth.config.LoginMemberArgumentResolverConfig;
import roomescape.common.auth.interceptor.AuthenticationInterceptor;
import roomescape.common.auth.interceptor.AuthorizationInterceptor;
import roomescape.common.auth.resolver.LoginMemberArgumentResolver;
import roomescape.date.controller.ReservationDateController;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(
    controllers = ReservationDateController.class,
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LoginMemberArgumentResolver.class
        ),
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LoginMemberArgumentResolverConfig.class
        ),
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = AuthenticationInterceptor.class
        ),
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = AuthInterceptorConfig.class
        ),
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = AuthorizationInterceptor.class
        )
    }
)
@Import(TestArgumentResolverConfig.class)
public @interface ControllerSliceTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
