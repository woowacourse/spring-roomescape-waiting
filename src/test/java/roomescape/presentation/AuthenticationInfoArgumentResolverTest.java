package roomescape.presentation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.auth.AuthenticationTokenHandler;
import roomescape.domain.user.UserRole;
import roomescape.exception.AuthenticationException;
import roomescape.exception.AuthorizationException;
import roomescape.presentation.auth.AuthenticationInfoArgumentResolver;
import roomescape.presentation.auth.AuthenticationTokenCookie;

class AuthenticationInfoArgumentResolverTest {

    private final AuthenticationTokenHandler tokenHandler = Mockito.mock(AuthenticationTokenHandler.class);
    private final MockMvc mockMvc = standaloneSetup(new TestController())
        .setCustomArgumentResolvers(new AuthenticationInfoArgumentResolver(tokenHandler))
        .build();

    @Test
    @DisplayName("인증 정보를 바인딩할 때 쿠키에 유효한 토큰이 있으면 바인딩된다.")
    void bindUserWhenRequestWithValidToken() throws Exception {
        var cookie = AuthenticationTokenCookie.forResponse("validToken");
        Mockito.when(tokenHandler.extractAuthenticationInfo("validToken")).thenReturn(new AuthenticationInfo(1L, UserRole.USER));

        mockMvc.perform(get("/authenticationInfoTest").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("인증 정보를 바인딩할 때 쿠키에 토큰이 없으면 예외가 발생한다.")
    void bindUserWhenRequestWithoutToken() {
        assertThatThrownBy(() -> mockMvc.perform(get("/authenticationInfoTest")))
                .hasCauseInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("유저를 바인딩할 때 쿠키에 유효하지 않은 토큰이 있으면 예외가 발생한다.")
    void bindUserWhenRequestWithInvalidToken() {
        var invalidToken = "invalidToken";
        Mockito.when(tokenHandler.extractAuthenticationInfo(invalidToken)).thenThrow(new AuthorizationException("invalid token"));

        assertThatThrownBy(() ->
            mockMvc.perform(get("/authenticationInfoTest")
                .cookie(AuthenticationTokenCookie.forResponse(invalidToken)))
        ).hasCauseInstanceOf(AuthorizationException.class);
    }

    @Controller
    private static class TestController {

        @GetMapping("/authenticationInfoTest")
        public ResponseEntity<AuthenticationInfo> test(final AuthenticationInfo authenticationInfo) {
            return ResponseEntity.ok(authenticationInfo);
        }
    }
}
