package roomescape.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import roomescape.dto.request.LoginRequestDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.presentation.support.CookieUtils;
import roomescape.service.AuthService;

@WebMvcTest({AuthController.class, CookieUtils.class})
public class AuthControllerTest {

    @MockitoBean
    AuthService authService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("정상적인 사용자가 로그인 시 토큰을 쿠키에 담아 반환한다.")
    void test1() throws Exception {
        // given
        String email = "email@gmail.com";
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, "password");

        // when
        String token = "exampleToken";
        Mockito.when(authService.createToken(email)).thenReturn(new TokenResponseDto(token));

        // then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("token", token));
    }
}
