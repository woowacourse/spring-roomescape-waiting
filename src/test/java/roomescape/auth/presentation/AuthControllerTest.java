package roomescape.auth.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.auth.fixture.TokenFixture.DUMMY_TOKEN;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.AdminHandlerInterceptor;
import roomescape.auth.AuthenticatedMemberArgumentResolver;
import roomescape.auth.dto.Accessor;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.service.AuthService;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthenticatedMemberArgumentResolver authenticatedMemberArgumentResolver;

    @MockBean
    private AdminHandlerInterceptor adminHandlerInterceptor;

    @DisplayName("로그인 요청 성공 시 응답 쿠키에 토큰을 포함시킨다")
    @Test
    void should_include_cookie_when_login_request_success() throws Exception {
        LoginRequest loginRequest = new LoginRequest("kk@gmail.com", "123");
        when(authService.login(any(LoginRequest.class))).thenReturn(DUMMY_TOKEN);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"));
    }

    @DisplayName("로그인 확인 요청 시 유효한 토큰 값이면 멤버의 이름을 반환한다")
    @Test
    void should_return_name_when_login_check_status_is_valid() throws Exception {
        MockCookie mockCookie = new MockCookie("token", "mock value");
        LoginCheckResponse loginCheckResponse = new LoginCheckResponse("리비");
        when(authService.checkLogin(any(Accessor.class))).thenReturn(loginCheckResponse);

        mockMvc.perform(get("/login/check")
                        .cookie(mockCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("리비"));
    }

    @DisplayName("로그 아웃 요청 시 쿠키를 만료시켜 로그 아웃한다")
    @Test
    void should_expire_cookie_when_logout_requested() throws Exception {
        MockCookie mockCookie = new MockCookie("token", "mock value");

        mockMvc.perform(post("/logout")
                        .cookie(mockCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("token", 0));
    }
}
