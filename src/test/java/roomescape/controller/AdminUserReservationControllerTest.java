package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.http.MediaType;
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
import roomescape.global.auth.AdminAuthorizationInterceptor;
import roomescape.global.exception.DomainErrorHttpMapper;
import roomescape.global.config.WebConfig;
import roomescape.service.AuthService;
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationWithWaitingOrder;

@WebMvcTest(AdminReservationController.class)
@Import({DomainErrorHttpMapper.class, AdminAuthorizationInterceptor.class, WebConfig.class})
class AdminUserReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AuthService authService;

    @DisplayName("관리자는 모든 예약 목록을 조회한다.")
    @Test
    void findAll() throws Exception {
        given(authService.getLoginMember(7L)).willReturn(admin());
        given(reservationService.findAll()).willReturn(List.of(
                new ReservationWithWaitingOrder(reservation(), 0)
        ));

        mockMvc.perform(get("/admin/reservations").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(1))
                .andExpect(jsonPath("$[0].status").value("RESERVED"))
                .andExpect(jsonPath("$[0].time").value("10:00"));
    }

    @DisplayName("관리자 예약 생성은 201과 Location 헤더를 반환한다.")
    @Test
    void create() throws Exception {
        given(authService.getLoginMember(7L)).willReturn(admin());
        given(reservationService.saveReservationByAdmin(any())).willReturn(1L);

        mockMvc.perform(post("/admin/reservations")
                        .session(adminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberId": 1,
                                  "date": "2026-07-01",
                                  "timeId": 1,
                                  "themeId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/1"));
    }

    @DisplayName("관리자 예약 취소는 예약 ID만으로 서비스에 위임한다.")
    @Test
    void cancel() throws Exception {
        given(authService.getLoginMember(7L)).willReturn(admin());

        mockMvc.perform(delete("/admin/reservations/1")
                        .session(adminSession()))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservationByAdmin(1L);
    }

    @DisplayName("존재하지 않는 예약이면 404를 반환한다.")
    @Test
    void cancelNotFound() throws Exception {
        given(authService.getLoginMember(7L)).willReturn(admin());
        org.mockito.Mockito.doThrow(new RoomescapeException(
                        DomainErrorCode.NOT_FOUND_RESERVATION,
                        "해당 ID의 예약이 존재하지 않습니다."
                ))
                .when(reservationService)
                .cancelReservationByAdmin(404L);

        mockMvc.perform(delete("/admin/reservations/404")
                        .session(adminSession()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_RESERVATION"));
    }

    @DisplayName("관리자 세션이 없으면 관리자 예약 API를 사용할 수 없다.")
    @Test
    void unauthenticated() throws Exception {
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @DisplayName("일반 사용자는 관리자 예약 API를 사용할 수 없다.")
    @Test
    void userForbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, 1L);
        given(authService.getLoginMember(1L)).willReturn(new Member(1L, "roro", "러로", "password", Role.USER));

        mockMvc.perform(get("/admin/reservations").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_ADMIN"));
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, 7L);
        return session;
    }

    private Member admin() {
        return new Member(7L, "admin", "관리자", "password", Role.ADMIN);
    }

    private Reservation reservation() {
        Member member = new Member(1L, "roro", "러로", "password", Role.USER);
        Schedule schedule = new Schedule(
                1L,
                new Theme(1L, "잠긴 방", "설명", "https://example.com/theme.jpg", 20000),
                LocalDate.of(2026, 7, 1),
                new ReservationTime(1L, LocalTime.of(10, 0))
        );
        return new Reservation(1L, member, schedule, ReservationStatus.RESERVED, LocalDateTime.now());
    }
}
