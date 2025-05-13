package roomescape.unit.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.AuthorizationExtractor;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.response.AuthenticatedUserResponse;
import roomescape.exception.InvalidTokenException;
import roomescape.infrastructure.JwtTokenProvider;
import roomescape.presentation.AuthController;
import roomescape.service.AuthService;

@WebMvcTest(value = {AuthController.class, AuthorizationExtractor.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 로그인에_성공하면_Set_Cookie로_토큰을_설정한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest("email@domain.com", "password1");
        given(authService.createToken(request)).willReturn("accessToken");
        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("token", "accessToken"));
    }

    @Test
    void 인증_정보를_조회한다() throws Exception {
        // given
        AuthenticatedUserResponse response = new AuthenticatedUserResponse("name1");
        given(authService.getAuthenticatedUser(1L)).willReturn(response);
        given(jwtTokenProvider.extractSubject("accessToken")).willReturn("1");
        // when & then
        mockMvc.perform(get("/api/auth/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", "accessToken")))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    void 잘못된_토큰으로_인증_정보를_조회하면_401에러를_반환한다() throws Exception {
        // given
        AuthenticatedUserResponse response = new AuthenticatedUserResponse("name1");
        given(authService.getAuthenticatedUser(1L)).willReturn(response);
        given(jwtTokenProvider.extractSubject("accessToken")).willThrow(new InvalidTokenException());
        // when & then
        mockMvc.perform(get("/api/auth/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", "accessToken")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그아웃하면_Set_Cookie로_토큰의_maxAge를_0으로_설정한다() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("token", "accesssToken")))
                .andExpect(status().isOk())
                .andExpect(cookie().value("token", ""))
                .andExpect((cookie().maxAge("token", 0)));
    }
}