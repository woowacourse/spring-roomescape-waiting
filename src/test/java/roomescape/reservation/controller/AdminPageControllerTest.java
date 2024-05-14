package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.auth.Role;
import roomescape.auth.TokenProvider;
import roomescape.config.ControllerConfig;
import roomescape.config.RoleCheckInterceptor;
import roomescape.exception.ExceptionPageController;
import roomescape.member.dto.LoginMemberInToken;

@WebMvcTest(AdminPageController.class)
@Import({ControllerConfig.class, ExceptionPageController.class})
class AdminPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminPageController())
                .setControllerAdvice(new ExceptionPageController())
                .addInterceptors(new RoleCheckInterceptor(tokenProvider))
                .build();
    }

    @Test
    @DisplayName("/admin 을 요청하면 index.html 를 반환한다.")
    void requestAdmin() throws Exception {
        LoginMemberInToken loginMemberInToken = new LoginMemberInToken(1L, Role.ADMIN, "어드민", "hogi@naver.com");
        doReturn(loginMemberInToken).when(tokenProvider)
                .getLoginMember(anyString());

        mockMvc.perform(get("/admin")
                        .cookie(new Cookie("token", "cookieValue")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"));
    }

    @Test
    @DisplayName("/admin/reservation 를 요청하면 admin/reservation-new.html 를 반환한다.")
    void requestAdminReservation() throws Exception {
        LoginMemberInToken loginMemberInToken = new LoginMemberInToken(1L, Role.ADMIN, "어드민", "hogi@naver.com");
        doReturn(loginMemberInToken).when(tokenProvider)
                .getLoginMember(anyString());

        mockMvc.perform(get("/admin/reservation")
                        .cookie(new Cookie("token", "cookieValue")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation-new"));
    }

    @Test
    @DisplayName("/admin/time을 요청하면 time.html 를 반환한다.")
    void requestTime() throws Exception {
        LoginMemberInToken loginMemberInToken = new LoginMemberInToken(1L, Role.ADMIN, "어드민", "hogi@naver.com");
        doReturn(loginMemberInToken).when(tokenProvider)
                .getLoginMember(anyString());

        mockMvc.perform(get("/admin/time")
                        .cookie(new Cookie("token", "cookieValue")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/time"));
    }

    @Test
    @DisplayName("/theme 을 요청하면 admin/theme.html 를 반환한다.")
    void requestTheme() throws Exception {
        LoginMemberInToken loginMemberInToken = new LoginMemberInToken(1L, Role.ADMIN, "어드민", "hogi@naver.com");
        doReturn(loginMemberInToken).when(tokenProvider)
                .getLoginMember(anyString());

        mockMvc.perform(get("/admin/theme")
                        .cookie(new Cookie("token", "cookieValue")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/theme"));
    }
}
