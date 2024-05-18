package roomescape.auth.presentation;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import roomescape.auth.application.AuthService;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.exception.AuthorizationException;
import roomescape.common.ControllerTest;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static roomescape.TestFixture.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends ControllerTest {
    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("로그인 요청 시 상태코드 200을 반환한다.")
    void login() throws Exception {
        // given
        String expectedToken = "token";
        LoginRequest request = new LoginRequest(MIA_EMAIL, TEST_PASSWORD);

        BDDMockito.given(authService.createToken(anyString(), anyString()))
                .willReturn(expectedToken);

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, "token=" + expectedToken));
    }

    @ParameterizedTest
    @MethodSource(value = "invalidRequests")
    @DisplayName("로그인 요청 시 아이디나 비밀번호가 비어있다면 상태코드 400을 반환한다.")
    void loginWithInvalidRequest(LoginRequest request) throws Exception {
        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    private static Stream<LoginRequest> invalidRequests() {
        return Stream.of(
                new LoginRequest(MIA_EMAIL, null),
                new LoginRequest(MIA_EMAIL, " "),
                new LoginRequest(MIA_EMAIL, ""),
                new LoginRequest(null, TEST_PASSWORD),
                new LoginRequest(" ", TEST_PASSWORD),
                new LoginRequest("", TEST_PASSWORD)
        );
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 요청 시 상태코드 401을 반환한다.")
    void loginWithNotExistingEmail() throws Exception {
        // given
        LoginRequest request = new LoginRequest("not existing email", TEST_PASSWORD);

        BDDMockito.willThrow(new AuthorizationException(TEST_ERROR_MESSAGE))
                .given(authService)
                .createToken(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("인증 정보 GET 요청 시 상태코드 200을 반환한다.")
    void checkAuthInformation() throws Exception {
        // given
        Cookie cookie = new Cookie("token", "token");

        BDDMockito.given(authService.extractMember(any()))
                .willReturn(USER_MIA());

        // when & then
        mockMvc.perform(get("/login/check")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(MIA_NAME));
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 인증 정보 GET 요청 시 상태코드 401을 반환한다.")
    void checkAuthInformationWithInvalidToken() throws Exception {
        // given
        Cookie cookie = new Cookie("token", "invalid-token");

        BDDMockito.willThrow(new AuthorizationException(TEST_ERROR_MESSAGE))
                .given(authService)
                .extractMember(any());

        // when & then
        mockMvc.perform(get("/login/check")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("토큰 없이 인증 정보 GET 요청 시 상태코드 401을 반환한다.")
    void checkAuthInformationWithoutToken() throws Exception {
        // when & then
        mockMvc.perform(get("/login/check"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }
}
