package roomescape.presentation.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.auth.AuthService;
import roomescape.common.auth.SessionKeys;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRole;
import roomescape.presentation.error.GlobalExceptionHandler;

@DisplayName("인증 컨트롤러")
@WebMvcTest(controllers = AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("로그인하면 세션에 사용자 정보가 저장된다")
    void login() throws Exception {
        // given
        User user = User.of(1L, "홍길동", "hashed", UserRole.USER);
        given(authService.login(any())).willReturn(user);

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "홍길동",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(request().sessionAttribute(SessionKeys.LOGIN_USER, user))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("회원가입하면 세션에 사용자 정보가 저장된다")
    void signup() throws Exception {
        // given
        User user = User.of(2L, "새사용자", "hashed", UserRole.USER);
        given(authService.signup(any())).willReturn(user);

        // when & then
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새사용자",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/2"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(request().sessionAttribute(SessionKeys.LOGIN_USER, user))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("새사용자"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("로그아웃하면 세션을 무효화한다")
    void logout() throws Exception {
        // when & then
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(delete("/logout").session(session))
                .andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
    }
}
