package roomescape.common.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.admin.interceptor.AdminInterceptor;
import roomescape.admin.presentation.AdminController;
import roomescape.common.util.JwtTokenContainer;
import roomescape.common.util.TokenCookieManager;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.service.ReservationService;

@ExtendWith(MockitoExtension.class)
class AdminInterceptorUnitTest {

    private TokenCookieManager tokenCookieManager = new TokenCookieManager();
    private JwtTokenContainer jwtTokenContainer = new JwtTokenContainer("sdfsdafsadfsadfsdafsadfsafsadfsldajfsdajf");
    private AdminInterceptor adminInterceptor = new AdminInterceptor(tokenCookieManager, jwtTokenContainer);

    @Mock
    private ReservationService reservationService;

    @Test
    @DisplayName("로그인이 안된 경우에는 login 페이지로 리다이렉트가 된다.")
    void preHandle_when_no_login() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/reservations");
        Method method = AdminController.class.getMethod("createReservation", AdminReservationRequest.class);
        HandlerMethod handlerMethod = new HandlerMethod(new AdminController(reservationService), method);
        MockHttpServletResponse response = new MockHttpServletResponse();
        // when
        boolean check = adminInterceptor.preHandle(request, response, handlerMethod);
        // then
        assertThat(check).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/login");
    }

    @Test
    @DisplayName("유효하지 않은 토큰이 들어온 경우 로그인 페이지로 이동한다.")
    void preHandler_when_invalid_token() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/reservations");
        Method method = AdminController.class.getMethod("createReservation", AdminReservationRequest.class);
        HandlerMethod handlerMethod = new HandlerMethod(new AdminController(reservationService), method);
        MockHttpServletResponse response = new MockHttpServletResponse();
        String strangeToken = "Asdasdasd";
        putCookieToRequest(strangeToken, request);
        // when
        boolean check = adminInterceptor.preHandle(request, response, handlerMethod);
        // then
        assertThat(check).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/login");
    }

    @Test
    @DisplayName("만료된 토큰이 들어온 경우 로그인 페이지로 이동한다.")
    void preHandler_when_expired_token() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/reservations");
        Method method = AdminController.class.getMethod("createReservation", AdminReservationRequest.class);
        HandlerMethod handlerMethod = new HandlerMethod(new AdminController(reservationService), method);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Member member = Member.createWithId(1L, "a", "a", "a", Role.ADMIN);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.of(2000, 11, 2, 12, 34));
        putCookieToRequest(jwtToken, request);
        // when
        boolean check = adminInterceptor.preHandle(request, response, handlerMethod);
        // then
        assertThat(check).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/login");
    }

    @Test
    @DisplayName("유효한 토큰이지만 admin 유저가 아닌 경우 403 반환한다.")
    void preHandler_when_not_admin() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/reservations");
        Method method = AdminController.class.getMethod("createReservation", AdminReservationRequest.class);
        HandlerMethod handlerMethod = new HandlerMethod(new AdminController(reservationService), method);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Member member = Member.createWithId(1L, "a", "a", "a", Role.USER);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.now());
        putCookieToRequest(jwtToken, request);
        // when
        boolean check = adminInterceptor.preHandle(request, response, handlerMethod);
        // then
        assertThat(check).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("유요한 토큰이고 admin 유저인 경우 true를 반환한다.")
    void preHandler_when_admin() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/reservations");
        Method method = AdminController.class.getMethod("createReservation", AdminReservationRequest.class);
        HandlerMethod handlerMethod = new HandlerMethod(new AdminController(reservationService), method);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Member member = Member.createWithId(1L, "a", "a", "a", Role.ADMIN);
        String jwtToken = jwtTokenContainer.createJwtToken(member, LocalDateTime.now());
        putCookieToRequest(jwtToken, request);
        // when
        boolean check = adminInterceptor.preHandle(request, response, handlerMethod);
        // then
        assertThat(check).isTrue();
    }

    private void putCookieToRequest(String strangeToken, MockHttpServletRequest request) {
        Cookie cookie = new Cookie("token", strangeToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        request.setCookies(cookie);
    }
}