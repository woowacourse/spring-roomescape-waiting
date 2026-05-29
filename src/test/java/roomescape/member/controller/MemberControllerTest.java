package roomescape.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
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
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.service.MemberService;

@ControllerSliceTest(controllers = MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;


    @Nested
    @DisplayName("register 메서드는")
    class RegisterTest {

        @Test
        @DisplayName("회원가입에 성공하면 201을 반환한다")
        void success() throws Exception {
            String name = "name";
            String password = "1234";
            Member member = Member.load(1L, name, password, Role.MEMBER);
            when(memberService.register(any()))
                .thenReturn(member);

            String request = """
                {
                    "name": "%s",
                    "password": "%s"
                }
                """.formatted(name, password);

            mockMvc.perform(post("/member/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.name").value(member.getName()))
                .andExpect(jsonPath("$.role").value(member.getRole().name()));

        }


        @Test
        @DisplayName("이름이 null이면 400을 반환한다")
        void failedByNameNull() throws Exception {
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

            verifyNoInteractions(memberService);
        }


        @Test
        @DisplayName("password가 null이면 400을 반환한다")
        void failedByPasswordNull() throws Exception {
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

            verifyNoInteractions(memberService);
        }
    }
}
