package roomescape.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.common.GlobalExceptionHandler;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberTokenResponse;
import roomescape.member.login.authentication.WebMvcConfiguration;
import roomescape.member.login.authorization.JwtTokenProvider;
import roomescape.member.login.authorization.LoginAuthorizationInterceptor;
import roomescape.member.login.authorization.TokenAuthorizationHandler;
import roomescape.member.service.MemberService;

@WebMvcTest(LoginApiController.class)
@Import({WebMvcConfiguration.class, GlobalExceptionHandler.class})
class LoginApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private TokenAuthorizationHandler tokenAuthorizationHandler;
    @MockitoBean
    private LoginAuthorizationInterceptor loginAuthorizationInterceptor;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String URI = "/login";

    @DisplayName("로그인 요청을 처리한다")
    @Test
    void login() throws Exception {
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        when(memberService.createToken(any(MemberLoginRequest.class)))
                .thenReturn(new MemberTokenResponse("test-token"));

        mockMvc.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @DisplayName("로그인 요청 시 이메일이 빈 값인 경우 잘못된 요청으로 처리한다")
    @Test
    void exception_login_empty_email() throws Exception {
        String requestBody = """
                {
                    "email": "",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("로그인 상태를 확인한다")
    @Test
    void checkLogin() throws Exception {
        when(memberService.findByToken("test-token"))
                .thenReturn(new MemberResponse(1L, "test-user", "test@example.com"));

        mockMvc.perform(get(URI + "/check")
                        .cookie(new Cookie("token", "test-token"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
