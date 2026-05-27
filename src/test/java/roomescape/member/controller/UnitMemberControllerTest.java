package roomescape.member.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.common.auth.jwt.JwtExtractor;
import roomescape.common.auth.jwt.JwtProvider;
import roomescape.common.auth.jwt.JwtValidator;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class UnitMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtValidator jwtValidator;

    @MockitoBean
    private JwtExtractor jwtExtractor;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private MemberRepository memberRepository;


    @Nested
    @DisplayName("register 메서드는")
    class RegisterTest {


        @Test
        @DisplayName("이름이 null이면 400을 반환한다")
        void 실패1() throws Exception {
            String nullNameRequest = """
                {
                  "name": null,
                  "password": "1234"
                }
                """;

            mockMvc.perform(post("/member/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(nullNameRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidParams[0].name").value("name"))
                .andExpect(jsonPath("$.invalidParams[0].reason").value("name을 입력해주세요."));

            verifyNoInteractions(memberRepository);
            verifyNoInteractions(memberService);
        }


        @Test
        @DisplayName("register null password")
        void 실패2() throws Exception {
            String nullPasswordRequest = """
                {
                  "name": "송송",
                  "password": null
                }
                """;

            mockMvc.perform(post("/member/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(nullPasswordRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidParams[0].name").value("password"))
                .andExpect(jsonPath("$.invalidParams[0].reason").value("비밀번호를 입력해주세요."));

            verifyNoInteractions(memberRepository);
            verifyNoInteractions(memberService);
        }
    }
}
