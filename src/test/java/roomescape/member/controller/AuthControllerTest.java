package roomescape.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.common.annotation.ControllerSliceTest;
import roomescape.member.service.AuthService;

@ControllerSliceTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Nested
    @DisplayName("login 메서드는")
    class LoginTest {

        @Test
        @DisplayName("로그인에 성공하면 200을 반환한다")
        void loginSuccess() throws Exception {
            String name = "name";
            String password = "password";
            String token = "token";

            when(authService.login(any()))
                .thenReturn(token);
            String request = """
                {
                    "name": "%s",
                    "password": "%s"
                }
                """.formatted(name, password);

            mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
        }
    }

}
