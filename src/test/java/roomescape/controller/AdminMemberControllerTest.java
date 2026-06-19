package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.global.auth.AdminAuthorizationInterceptor;
import roomescape.global.exception.DomainErrorHttpMapper;
import roomescape.global.config.WebConfig;
import roomescape.service.AuthService;
import roomescape.service.MemberService;

@WebMvcTest(AdminMemberController.class)
@Import({DomainErrorHttpMapper.class, AdminAuthorizationInterceptor.class, WebConfig.class})
class AdminMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private AuthService authService;

    @DisplayName("관리자는 일반 회원 목록을 조회한다.")
    @Test
    void findUsers() throws Exception {
        given(authService.getLoginMember(7L)).willReturn(new Member(7L, "admin", "관리자", "password", Role.ADMIN));
        given(memberService.findUsers()).willReturn(List.of(
                new Member(1L, "roro", "러로", "password", Role.USER)
        ));

        mockMvc.perform(get("/admin/members").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].loginId").value("roro"))
                .andExpect(jsonPath("$[0].name").value("러로"))
                .andExpect(jsonPath("$[0].role").value("USER"));
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, 7L);
        return session;
    }
}
