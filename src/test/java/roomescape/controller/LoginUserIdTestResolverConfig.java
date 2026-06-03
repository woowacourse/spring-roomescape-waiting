package roomescape.controller;

import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.domain.User;
import roomescape.fixture.Fixtures;
import roomescape.infrastructure.LoginUser;

@TestConfiguration
public class LoginUserIdTestResolverConfig implements WebMvcConfigurer {

    public static final User FIXED_USER = Fixtures.memberWithId(1L, "브라운");

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(LoginUser.class)
                        && parameter.getParameterType().equals(User.class);
            }

            @Override
            public Object resolveArgument(
                    MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return FIXED_USER;
            }
        });
    }
}
