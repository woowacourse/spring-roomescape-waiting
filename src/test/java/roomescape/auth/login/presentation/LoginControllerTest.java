package roomescape.auth.login.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.login.presentation.dto.LoginRequest;
import roomescape.auth.login.service.LoginService;
import roomescape.auth.token.JwtTokenManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({MemberLoginController.class, AdminLoginController.class})
@Import({LoginService.class, JwtTokenManager.class})
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 회원_로그인_성공() throws Exception {
        LoginRequest request = new LoginRequest("email@email.com", "password");
        String token = "jwt-token";

        given(loginService.createMemberToken(any()))
            .willReturn(token);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("token"));
    }

    @Test
    void 관리자_로그인_성공() throws Exception {
        LoginRequest request = new LoginRequest("admin", "password");
        String token = "jwt-token";

        given(loginService.createAdminToken(any()))
            .willReturn(token);

        mockMvc.perform(post("/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("token"));
    }
} 