package roomescape.controller.view;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.domain.MemberRole;
import roomescape.service.MemberService;
import roomescape.service.result.MemberResult;

@WebMvcTest(AdminViewController.class)
@Import({CookieProvider.class, JwtTokenProvider.class})
class AdminViewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MemberService memberService;

    private Cookie getAdminCookie() {
        MemberResult memberResult = new MemberResult(1L, "히스타", MemberRole.ADMIN, "admin@email.com");
        String token = jwtTokenProvider.createToken(memberResult);
        return new Cookie("token", token);
    }

    @DisplayName("유저 권한으로 어드민 페이지를 접속할 시 403 Forbidden 에러가 발생한다.")
    @Test
    void adminIndexWithUser() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("어드민 권한으로 어드민 페이지를 접속할 시 성공한다.")
    @Test
    void adminIndexWithAdmin() throws Exception {
        mockMvc.perform(get("/admin").cookie(getAdminCookie()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"));
    }

    @DisplayName("/reservation 어드민 권한으로 접속 테스트")
    @Test
    void reservationWithAdmin() throws Exception {
        mockMvc.perform(get("/admin/reservation").cookie(getAdminCookie()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation-new"));
    }

    @DisplayName("/reservation-waiting 어드민 권한으로 접속 테스트")
    @Test
    void reservationWaitingWithAdmin() throws Exception {
        mockMvc.perform(get("/admin/reservation-waiting").cookie(getAdminCookie()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/waiting"));
    }

    @DisplayName("/time 어드민 권한으로 접속 테스트")
    @Test
    void timeWithAdmin() throws Exception {
        mockMvc.perform(get("/admin/time").cookie(getAdminCookie()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/time"));
    }

    @DisplayName("/theme 어드민 권한으로 접속 테스트")
    @Test
    void themeWithAdmin() throws Exception {
        mockMvc.perform(get("/admin/theme").cookie(getAdminCookie()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/theme"));
    }
}
