package roomescape.common.interceptor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.admin.presentation.AdminPageController;
import roomescape.common.util.JwtTokenContainer;
import roomescape.common.util.TokenCookieManager;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.service.LoginService;
import roomescape.reservation.presentation.ReservationPageController;

@WebMvcTest({AdminPageController.class, ReservationPageController.class})
@Import({TokenCookieManager.class, JwtTokenContainer.class})
class AdminInterceptorTest {

    @Autowired
    private JwtTokenContainer jwtTokenContainer;

    @MockitoBean
    private LoginService loginService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("admin와 관련되지 않은 페이지일 경우 적용되지 않는다.")
    void not_admin_page() throws Exception {
        mockMvc.perform(get("/reservation"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("admin 관련 페이지인데 토큰이 없는 경우 login page로 이동한다.")
    void admin_page_when_no_token() throws Exception {
        mockMvc.perform(get("/admin/reservation"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("admin 관련 페이지인데 토큰이 만료된 경우 login page로 이동한다.")
    void admin_page_when_expired_token() throws Exception {
        // given
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.of(2000, 11, 2, 12, 0));
        Cookie cookie = new Cookie("token", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        // when & then
        mockMvc.perform(get("/admin/reservation").cookie(cookie))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("admin 관련 페이지인데 일반 회원일 경우 403을 반환한다.")
    void admin_page_when_user() throws Exception {
        // given
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.now());
        Cookie cookie = new Cookie("token", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        // when & then
        mockMvc.perform(get("/admin/reservation").cookie(cookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("admin 관련 페이지에서 어드민인 경우 정상적으로 반환한다.")
    void admin_page_when_admin() throws Exception {
        // given
        Member member = Member.createWithId(1L, "a", "a", "a", Role.ADMIN);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.now());
        Cookie cookie = new Cookie("token", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        // when & then
        mockMvc.perform(get("/admin/reservation").cookie(cookie))
                .andExpect(status().isOk());
    }

}