package roomescape.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignupRequest;
import roomescape.member.login.authorization.LoginAuthorizationInterceptor;
import roomescape.member.login.authorization.TokenAuthorizationHandler;
import roomescape.member.service.MemberService;

@WebMvcTest(MemberApiController.class)
class MemberApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private TokenAuthorizationHandler tokenAuthorizationHandler;
    @MockitoBean
    private LoginAuthorizationInterceptor loginAuthorizationInterceptor;

    private static final String URI = "/members";

    @DisplayName("모든 회원 정보를 조회한다")
    @Test
    void findAll() throws Exception {
        when(memberService.findAll()).thenReturn(List.of());

        mockMvc.perform(get(URI)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("회원가입 요청을 처리한다")
    @Test
    void add() throws Exception {
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "1234",
                    "name": "test-user"
                }
                """;

        when(memberService.add(any(MemberSignupRequest.class)))
                .thenReturn(new MemberResponse(1L, "test-user", "test@example.com"));

        mockMvc.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}
