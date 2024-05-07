package roomescape.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberLoginResponse;
import roomescape.member.security.service.MemberAuthService;
import roomescape.member.service.MemberService;

@WebMvcTest(MemberLoginController.class)
class MemberLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private MemberAuthService memberAuthService;

    @Test
    @DisplayName("로그인 프로세스가 정상적으로 수행되는지 확인한다")
    void loginProcess() throws Exception {
        MemberLoginRequest memberRequest = new MemberLoginRequest("user@example.com", "password");
        Member memberInfo = new Member("User", "user@example.com", "password");
        String token = "mocked-token";

        when(memberService.findMember(any())).thenReturn(memberInfo);
        when(memberAuthService.publishToken(any())).thenReturn(token);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(memberRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("token", true))
                .andExpect(cookie().exists("token"));
    }

    @Test
    @DisplayName("쿠키를 통해 사용자 인증 상태를 검사하는 요청을 정상적으로 처리한다")
    void checkLoginStatus() throws Exception {
        String memberName = "User";
        Cookie[] cookies = {new Cookie("token", "mocked-token")};

        when(memberAuthService.extractNameFromPayload(cookies)).thenReturn(memberName);

        mockMvc.perform(get("/login/check")
                        .cookie(cookies))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(result -> assertEquals(memberName,
                        new ObjectMapper().readValue(result.getResponse()
                                        .getContentAsString(), MemberLoginResponse.class)
                                .name()));
    }

}
