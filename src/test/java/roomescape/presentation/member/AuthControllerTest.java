package roomescape.presentation.member;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import roomescape.application.auth.TokenManager;
import roomescape.application.member.MemberService;
import roomescape.application.member.dto.request.MemberLoginRequest;
import roomescape.application.member.dto.response.TokenResponse;
import roomescape.presentation.ControllerTest;
import roomescape.presentation.auth.CredentialContext;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends ControllerTest {

    @MockBean
    private MemberService memberService;

    @MockBean
    private TokenManager tokenManager;

    @MockBean
    private CredentialContext context;

    @Test
    @DisplayName("로그인 시, 토큰이 담긴 쿠키를 반환한다.")
    void cookieOnLoginTest() throws Exception {
        MemberLoginRequest request = new MemberLoginRequest("test@mail.com", "1234");
        given(memberService.login(request))
                .willReturn(new TokenResponse("this-is-token"));
        String requestJson = objectMapper.writeValueAsString(request);

        Cookie[] cookies = mvc.perform(post("/login")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn()
                .getResponse()
                .getCookies();

        assertAll(
                () -> assertThat(cookies).extracting(Cookie::getName).contains("token"),
                () -> assertThat(cookies).extracting(Cookie::getValue).contains("this-is-token")
        );
    }
}
