package roomescape.presentation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.auth.AuthenticationTokenHandler;
import roomescape.domain.user.UserRole;
import roomescape.exception.AuthenticationException;
import roomescape.exception.AuthorizationException;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.auth.AuthenticationTokenCookie;
import roomescape.presentation.auth.UserArgumentResolver;

@ExtendWith(MockitoExtension.class)
class UserArgumentResolverTest {

    @Mock
    private AuthenticationTokenHandler tokenHandler;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new TestController())
                .setCustomArgumentResolvers(new UserArgumentResolver(tokenHandler))
                .build();
    }

    @Test
    @DisplayName("@Authenticated 인증 정보를 바인딩할 때 쿠키에 유효한 토큰이 있으면 바인딩된다.")
    void bindUserWhenRequestWithValidToken() throws Exception {
        var cookie = AuthenticationTokenCookie.forResponse("validToken");
        Mockito.when(tokenHandler.extractAuthenticationInfo("validToken")).thenReturn(new AuthenticationInfo(1L, UserRole.USER));

        mockMvc.perform(get("/authenticatedInfo").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": 1,
                          "role": "USER",
                        }
                        """)
                );
    }

    @Test
    @DisplayName("@Authenticated 인증 정보를 바인딩할 때 쿠키에 토큰이 없으면 예외가 발생한다.")
    void bindUserWhenRequestWithoutToken() {
        assertThatThrownBy(() -> mockMvc.perform(get("/authenticatedInfo")))
                .hasCauseInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("@Authenticated 유저를 바인딩할 때 쿠키에 유효하지 않은 토큰이 있으면 예외가 발생한다.")
    void bindUserWhenRequestWithInvalidToken() {
        var invalidTokenCookie = AuthenticationTokenCookie.forResponse("invalidToken");
        Mockito.when(tokenHandler.extractAuthenticationInfo("invalidToken"))
                .thenThrow(new AuthorizationException("invalid token"));
        assertThatThrownBy(() -> mockMvc.perform(get("/authenticatedInfo").cookie(invalidTokenCookie)))
                .hasCauseInstanceOf(AuthorizationException.class);
    }

    @Controller
    private static class TestController {

        @GetMapping("/authenticatedInfo")
        public ResponseEntity<AuthenticationInfo> test(@Authenticated AuthenticationInfo authenticationInfo) {
            return ResponseEntity.ok(authenticationInfo);
        }
    }
}
