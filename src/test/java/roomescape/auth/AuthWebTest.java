package roomescape.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.AuthWebTest.AuthTestConfig;
import roomescape.auth.AuthWebTest.AuthTestController;
import roomescape.auth.annotation.Authenticated;
import roomescape.auth.annotation.LoginName;

@WebMvcTest(AuthTestController.class)
@Import({AuthTestConfig.class, AuthTestController.class})
public class AuthWebTest {

    @Autowired
    MockMvc mockMvc;

    @DisplayName("@Authenticated 요청은 Authorization 헤더의 이름을 @LoginName 파라미터로 전달한다.")
    @Test
    void authenticated_success() throws Exception {
        mockMvc.perform(
                post("/test/auth")
                        .header(HttpHeaders.AUTHORIZATION, "brown")
                ).andExpect(status().isOk())
                .andExpect(content().json("\"brown\""));
    }

    @DisplayName("@Authenticated 요청에 Authorization 헤더가 없으면 401을 반환한다.")
    @Test
    void authenticated_without_authorization() throws Exception {
        mockMvc.perform(
                post("/test/auth")
                ).andExpect(status().isUnauthorized());
    }

    @DisplayName("@LoginName만 사용하면 인증 정보가 없어 401을 반환한다.")
    @Test
    void login_name_without_authenticated() throws Exception {
        mockMvc.perform(
                get("/test/login-name-only")
                        .header(HttpHeaders.AUTHORIZATION, "brown")
                ).andExpect(status().isUnauthorized());
    }

    @RestController
    public static class AuthTestController {

        @Authenticated
        @PostMapping("/test/auth")
        ResponseEntity<String> authenticate(@LoginName String name) {
            return ResponseEntity.ok(name);
        }

        @GetMapping("/test/login-name-only")
        ResponseEntity<String> loginNameOnly(@LoginName String name) {
            return ResponseEntity.ok(name);
        }
    }

    @TestConfiguration
    static class AuthTestConfig implements WebMvcConfigurer {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new NameAuthenticationInterceptor());
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new LoginNameArgumentResolver());
        }
    }
}
