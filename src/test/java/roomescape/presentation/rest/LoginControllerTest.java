package roomescape.presentation.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.application.AuthenticationService;
import roomescape.application.UserService;
import roomescape.exception.AuthenticationException;
import roomescape.presentation.GlobalExceptionHandler;
import roomescape.presentation.auth.AuthenticationTokenCookie;

class LoginControllerTest {

    private final AuthenticationService authenticationService = Mockito.mock(AuthenticationService.class);
    private final UserService userService = Mockito.mock(UserService.class);
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new LoginController(authenticationService, userService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();

    @Test
    @DisplayName("이메일과 비밀번호로 로그인 시 토큰을 쿠키로 발급받는다.")
    void performLogin() throws Exception {
        var expectedToken = "token";
        Mockito.when(authenticationService.issueToken("admin@email.com", "password"))
            .thenReturn(expectedToken);

        mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "email": "admin@email.com",
                    "password": "password"
                }
                """))
            .andExpect(cookie().value(AuthenticationTokenCookie.COOKIE_KEY, expectedToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일이나 비밀번호가 일치하지 않는 경우 로그인에 실패한다.")
    void performLoginWithInvalidEmail() throws Exception {
        Mockito.when(authenticationService.issueToken("admin@email.com", "ppp"))
            .thenThrow(new AuthenticationException("wrong password"));

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "email": "admin@email.com",
                    "password": "ppp"
                }
                """))
            .andExpect(cookie().doesNotExist(AuthenticationTokenCookie.COOKIE_KEY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 시 토큰 쿠키가 삭제되고 메인 페이지로 리다이렉트 된다")
    void performLogout() throws Exception {
        mockMvc.perform(post("/logout")
                .cookie(AuthenticationTokenCookie.forResponse("token")))
            .andExpect(cookie().maxAge(AuthenticationTokenCookie.COOKIE_KEY, 0))
            .andExpect(header().string("Location", "/"))
            .andExpect(status().isFound());
    }
}
