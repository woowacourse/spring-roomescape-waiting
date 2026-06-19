package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.global.exception.DomainErrorHttpMapper;
import roomescape.global.auth.LoginMemberArgumentResolver;
import roomescape.global.config.WebConfig;
import roomescape.service.AuthService;
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationWithWaitingOrder;

@WebMvcTest(UserReservationController.class)
@Import({DomainErrorHttpMapper.class, LoginMemberArgumentResolver.class, WebConfig.class})
class UserReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AuthService authService;

    @DisplayName("사용자 예약 목록을 JSON으로 반환한다.")
    @Test
    void findMine() throws Exception {
        Member member = member();
        given(authService.getLoginMember(1L)).willReturn(member);
        given(reservationService.findByMember(member)).willReturn(List.of(
                new ReservationWithWaitingOrder(reservation(member), 1)
        ));

        mockMvc.perform(get("/reservations").session(loginSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(1))
                .andExpect(jsonPath("$[0].name").value("러로"))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].date").value("2026-07-01"))
                .andExpect(jsonPath("$[0].themeName").value("잠긴 방"))
                .andExpect(jsonPath("$[0].time").value("10:00"))
                .andExpect(jsonPath("$[0].order").value(1));
    }

    @DisplayName("사용자는 로그인 세션으로 본인 예약을 취소한다.")
    @Test
    void cancel() throws Exception {
        Member member = member();
        given(authService.getLoginMember(1L)).willReturn(member);

        mockMvc.perform(delete("/reservations/1").session(loginSession()))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(1L, member);
    }

    @DisplayName("본인 예약이 아니면 403을 반환한다.")
    @Test
    void cancelUnauthorized() throws Exception {
        Member member = member();
        given(authService.getLoginMember(1L)).willReturn(member);
        org.mockito.Mockito.doThrow(new RoomescapeException(
                        DomainErrorCode.UNAUTHORIZED_RESERVATION,
                        "본인의 예약만 변경할 수 있습니다."
                ))
                .when(reservationService)
                .cancelReservation(1L, member);

        mockMvc.perform(delete("/reservations/1").session(loginSession()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_RESERVATION"));
    }

    @DisplayName("로그인하지 않으면 사용자 예약 목록을 조회할 수 없다.")
    @Test
    void unauthenticated() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    private MockHttpSession loginSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, 1L);
        return session;
    }

    private Member member() {
        return new Member(1L, "roro", "러로", "password", Role.USER);
    }

    private Reservation reservation(Member member) {
        Schedule schedule = new Schedule(
                1L,
                new Theme(1L, "잠긴 방", "닫힌 문을 여는 테마", "https://example.com/theme.jpg", 20000),
                LocalDate.of(2026, 7, 1),
                new ReservationTime(1L, LocalTime.of(10, 0))
        );
        return new Reservation(1L, member, schedule, ReservationStatus.WAITING, LocalDateTime.now());
    }
}
