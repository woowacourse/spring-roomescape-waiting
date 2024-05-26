package roomescape.util;

import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.controller.AuthController;
import roomescape.auth.controller.AuthPageController;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.handler.RequestHandler;
import roomescape.auth.handler.ResponseHandler;
import roomescape.auth.resolver.LoginUserArgumentResolver;
import roomescape.auth.service.AuthService;
import roomescape.auth.service.TokenProvider;
import roomescape.member.controller.AdminController;
import roomescape.member.controller.AdminPageController;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.ReservationController;
import roomescape.reservation.controller.ReservationPageController;
import roomescape.reservation.controller.ReservationTimeController;
import roomescape.reservation.controller.ThemeController;
import roomescape.reservation.service.ReservationApplicationService;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.reservation.service.ThemeService;

@WebMvcTest({
        LoginUserArgumentResolver.class,
        RequestHandler.class,
        ResponseHandler.class,
        AuthController.class,
        AuthPageController.class,
        AdminController.class,
        AdminPageController.class,
        ReservationController.class,
        ReservationPageController.class,
        ReservationTimeController.class,
        ThemeController.class
})
@ExtendWith(MockitoExtension.class)
public class ControllerTest {

    @MockBean
    protected TokenProvider tokenProvider;

    @MockBean
    protected AuthService authService;

    @MockBean
    protected ReservationApplicationService reservationApplicationService;

    @MockBean
    protected ReservationTimeService reservationTimeService;

    @MockBean
    protected ThemeService themeService;

    @MockBean
    protected MemberService memberService;

    @Autowired
    protected LoginUserArgumentResolver loginUserArgumentResolver;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    protected String adminToken = "adminToken";

    protected String memberToken = "chocoToken";

    @BeforeEach
    void setUp() {
        Member admin = new Member(10L, "관리자", "admin@roomescape.com", "admin", Role.ADMIN);
        doReturn(true)
                .when(tokenProvider)
                .isToken(adminToken);

        doReturn(AuthInfo.from(admin))
                .when(authService)
                .fetchByToken(adminToken);

        doReturn(admin)
                .when(memberService)
                .findById(admin.getId());

        Member member = new Member(11L, "초코칩", "dev.chocochip@gmail.com", "1234", Role.USER);
        doReturn(true)
                .when(tokenProvider)
                .isToken(memberToken);

        doReturn(AuthInfo.from(member))
                .when(authService)
                .fetchByToken(memberToken);

        doReturn(member)
                .when(memberService)
                .findById(member.getId());
    }
}
