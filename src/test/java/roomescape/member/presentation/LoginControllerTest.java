package roomescape.member.presentation;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.global.jwt.AuthorizationExtractor;
import roomescape.member.application.MemberService;
import roomescape.member.presentation.controller.LoginController;
import roomescape.member.presentation.dto.TokenRequest;
import roomescape.member.presentation.resolver.MemberArgumentResolver;

@WebMvcTest(value = LoginController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebMvcConfig.class,
                        AuthorizationInterceptor.class,
                        MemberArgumentResolver.class
                }
        ))
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private AuthorizationExtractor authorizationExtractor;

    @Test
    @DisplayName("로그인 성공 후 쿠키를 담아 반환한다")
    void loginSuccess() throws Exception {
        // given
        TokenRequest request = new TokenRequest("email@email.com", "password");
        String token = "token";

        doReturn(token).when(memberService)
                .createToken(request);

        // when && then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("잘못된 형식의 이메일로 로그인 시도시 400 응답을 반환한다")
    void loginFailInvalidEmail() throws Exception {
        // given
        TokenRequest request = new TokenRequest("invalid-email", "password");

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호가 비어있는 경우 400 응답을 반환한다")
    void loginFailEmptyPassword() throws Exception {
        // given
        TokenRequest request = new TokenRequest("email@email.com", "");

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일이 null인 경우 400 응답을 반환한다")
    void loginFailNullEmail() throws Exception {
        // given
        TokenRequest request = new TokenRequest(" ", "password");

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
