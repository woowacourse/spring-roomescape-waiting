//package roomescape.member.login.authentication;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.ArrayList;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import roomescape.auth.ui.AdminAuthorizationInterceptor;
//import roomescape.auth.ui.LoginMemberIdArgumentResolver;
//import roomescape.common.config.WebMvcConfiguration;
//
//class WebMvcConfigurationTest {
//
//    private WebMvcConfiguration webMvcConfiguration;
//    private LoginMemberIdArgumentResolver loginMemberIdArgumentResolver;
//    private AdminAuthorizationInterceptor adminAuthorizationInterceptor;
//
//    @BeforeEach
//    void setUp() {
//        loginMemberIdArgumentResolver = mock(LoginMemberIdArgumentResolver.class);
//        adminAuthorizationInterceptor = mock(AdminAuthorizationInterceptor.class);
//
//        webMvcConfiguration = new WebMvcConfiguration(
//                loginMemberIdArgumentResolver,
//                adminAuthorizationInterceptor
//        );
//    }
//
//    @Test
//    @DisplayName("인증 리졸버가 등록되어야 한다")
//    void shouldAddAuthenticationResolver() {
//        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
//
//        webMvcConfiguration.addArgumentResolvers(resolvers);
//
//        assertThat(resolvers.getFirst()).isEqualTo(loginMemberIdArgumentResolver);
//    }
//
//    @Test
//    @DisplayName("인터셉터가 /admin/** 경로에 등록되어야 한다")
//    void shouldAddAuthorizationInterceptor() {
//        InterceptorRegistry registry = mock(InterceptorRegistry.class);
//        InterceptorRegistration registration = mock(InterceptorRegistration.class);
//
//        when(registry.addInterceptor(any())).thenReturn(registration);
//        when(registration.addPathPatterns(any(String[].class))).thenReturn(registration);
//
//        webMvcConfiguration.addInterceptors(registry);
//
//        verify(registry).addInterceptor(adminAuthorizationInterceptor);
//        verify(registration).addPathPatterns(new String[]{"/admin/**"});
//    }
//}
