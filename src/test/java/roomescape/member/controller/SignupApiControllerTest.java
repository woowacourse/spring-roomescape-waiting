package roomescape.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.common.GlobalExceptionHandler;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignupRequest;
import roomescape.member.login.authentication.WebMvcConfiguration;
import roomescape.member.login.authorization.JwtTokenProvider;
import roomescape.member.login.authorization.LoginAuthorizationInterceptor;
import roomescape.member.login.authorization.TokenAuthorizationHandler;
import roomescape.member.service.MemberService;

@WebMvcTest(SignupApiController.class)
@Import({WebMvcConfiguration.class, GlobalExceptionHandler.class})
class SignupApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private TokenAuthorizationHandler tokenAuthorizationHandler;
    @MockitoBean
    private LoginAuthorizationInterceptor loginAuthorizationInterceptor;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String URI = "/signup";

    @DisplayName("회원가입 요청을 처리한다")
    @Test
    void signup() throws Exception {
        when(memberService.add(any(MemberSignupRequest.class)))
                .thenReturn(new MemberResponse(1L, "test-user", "test@example.com"));

        MemberSignupRequest signupRequest = new MemberSignupRequest("test@example.com", "1234", "test-user");
        String requestBody = objectMapper.writeValueAsString(signupRequest);

        mockMvc.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }
}
