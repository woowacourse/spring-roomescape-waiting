package roomescape.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.TestFixture;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.service.MemberService;
import roomescape.service.param.LoginMemberParam;
import roomescape.service.result.MemberResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CookieProvider cookieProvider;

    @MockitoBean
    private MemberService memberService;


    @Test
    @DisplayName("로그인에 성공하면 토큰이 담긴 쿠키와 함께 사용자 정보를 반환한다.")
    void loginSuccess() throws Exception {
        // given
        MemberResult memberResult = TestFixture.createMemberResult();

        when(memberService.login(any(LoginMemberParam.class))).thenReturn(memberResult);
        when(jwtTokenProvider.createToken(memberResult)).thenReturn(TestFixture.VALID_TOKEN);
        when(cookieProvider.create(TestFixture.VALID_TOKEN)).thenReturn(TestFixture.createAuthResponseCookie(TestFixture.VALID_TOKEN));

        // when & then
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestFixture.createLoginJson()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberResult.id()))
                .andExpect(jsonPath("$.name").value(memberResult.name()))
                .andExpect(jsonPath("$.email").value(memberResult.email()));
    }

    @Test
    @DisplayName("로그인 체크 시 유효한 토큰이 있으면 사용자 정보를 반환한다.")
    void checkLoginWithValidToken() throws Exception {
        // given
        MemberResult memberResult = TestFixture.createMemberResult();
        when(cookieProvider.extractTokenFromCookie(any())).thenReturn(TestFixture.VALID_TOKEN);
        when(jwtTokenProvider.extractIdFromToken(TestFixture.VALID_TOKEN)).thenReturn(TestFixture.TEST_MEMBER_ID);
        when(memberService.findById(TestFixture.TEST_MEMBER_ID)).thenReturn(memberResult);

        // when & then
        mockMvc.perform(get("/login/check")
                .cookie(TestFixture.createAuthCookie(TestFixture.VALID_TOKEN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(memberResult.name()));
    }

    @Test
    @DisplayName("로그아웃 시 쿠키가 삭제된다.")
    void logout() throws Exception {
        // given
        when(cookieProvider.invalidate(any())).thenReturn(TestFixture.createAuthResponseCookie(""));

        // when & then
        mockMvc.perform(post("/logout")
                .cookie(TestFixture.createAuthCookie(TestFixture.VALID_TOKEN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("token", -1));
    }
}