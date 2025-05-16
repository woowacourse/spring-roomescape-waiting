package roomescape.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.domain.MemberRole;
import roomescape.service.MemberService;
import roomescape.service.param.RegisterMemberParam;
import roomescape.service.result.MemberResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CookieProvider cookieProvider;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MemberService memberService;

    private static final String TEST_EMAIL = "test@email.com";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_NAME = "test";

    @Test
    @DisplayName("회원가입을 할 수 있다.")
    void signup() throws Exception {
        // given
        String signupJson = String.format("""
                {
                    "email": "%s",
                    "password": "%s",
                    "name": "%s"
                }
                """, TEST_EMAIL, TEST_PASSWORD, TEST_NAME);

        MemberResult memberResult = new MemberResult(1L, TEST_NAME, MemberRole.USER, TEST_EMAIL);
        when(memberService.create(any(RegisterMemberParam.class))).thenReturn(memberResult);

        // when & then
        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupJson)
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(memberResult.id()))
                .andExpect(jsonPath("$.name").value(memberResult.name()))
                .andExpect(jsonPath("$.email").value(memberResult.email()));
    }

    @Test
    @DisplayName("회원 정보 조회를 할 수 있다.")
    void findMembers() throws Exception {
        MemberResult memberResult = new MemberResult(1L, TEST_NAME, MemberRole.USER, TEST_EMAIL);
        when(memberService.findAll()).thenReturn(List.of(memberResult));

        mockMvc.perform(get("/members"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(memberResult.id()))
                .andExpect(jsonPath("$[0].name").value(memberResult.name()));
    }
}