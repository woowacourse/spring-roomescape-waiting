package roomescape.reservation.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import roomescape.auth.presentation.AdminAuthorizationInterceptor;
import roomescape.auth.presentation.LoginMemberArgumentResolver;
import roomescape.common.ControllerTest;
import roomescape.global.config.WebMvcConfiguration;
import roomescape.member.application.MemberService;
import roomescape.reservation.application.BookingQueryService;
import roomescape.reservation.application.ReservationManageService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.application.WaitingQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.AdminReservationSaveRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixture.MIA_NAME;
import static roomescape.TestFixture.MIA_RESERVATION;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;
import static roomescape.TestFixture.USER_MIA;
import static roomescape.TestFixture.WOOTECO_THEME;
import static roomescape.TestFixture.WOOTECO_THEME_NAME;
import static roomescape.common.StubLoginMemberArgumentResolver.STUBBED_LOGIN_MEMBER;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;

@WebMvcTest(
        value = AdminReservationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebMvcConfiguration.class, LoginMemberArgumentResolver.class, AdminAuthorizationInterceptor.class})
)
class AdminReservationControllerTest extends ControllerTest {
    @MockBean
    private BookingQueryService bookingQueryService;

    @MockBean
    private WaitingQueryService waitingQueryService;

    @MockBean
    @Qualifier("bookingManageService")
    private ReservationManageService bookingScheduler;

    @MockBean
    private MemberService memberService;

    @MockBean
    private ReservationTimeService reservationTimeService;

    @MockBean
    private ThemeService themeService;

    @Test
    @DisplayName("예약 POST 요청 시 상태코드 201을 반환한다.")
    void createReservation() throws Exception {
        // given
        AdminReservationSaveRequest request = new AdminReservationSaveRequest(MIA_RESERVATION_DATE, 1L, 1L, 1L);
        ReservationTime expectedTime = new ReservationTime(1L, MIA_RESERVATION_TIME);
        Theme expectedTheme = WOOTECO_THEME(1L);
        Reservation expectedReservation = MIA_RESERVATION(expectedTime, expectedTheme, USER_MIA(1L), BOOKING);

        BDDMockito.given(reservationTimeService.findById(anyLong()))
                .willReturn(expectedTime);
        BDDMockito.given(themeService.findById(anyLong()))
                .willReturn(expectedTheme);
        BDDMockito.given(memberService.findById(anyLong()))
                .willReturn(USER_MIA(1L));
        BDDMockito.given(bookingScheduler.scheduleRecentReservation(any()))
                .willReturn(expectedReservation);

        // when
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberName").value(MIA_NAME))
                .andExpect(jsonPath("$.time.id").value(1L))
                .andExpect(jsonPath("$.time.startAt").value(MIA_RESERVATION_TIME.toString()))
                .andExpect(jsonPath("$.date").value(MIA_RESERVATION_DATE.toString()));
    }

    @Test
    @DisplayName("예약 목록 GET 요청 시 상태코드 200을 반환한다.")
    void findReservations() throws Exception {
        // given
        ReservationTime expectedTime = new ReservationTime(1L, MIA_RESERVATION_TIME);
        Reservation expectedReservation = MIA_RESERVATION(expectedTime, WOOTECO_THEME(), USER_MIA(), BOOKING);

        BDDMockito.given(bookingQueryService.findAll())
                .willReturn(List.of(expectedReservation));

        // when & then
        mockMvc.perform(get("/admin/reservations").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberName").value(MIA_NAME))
                .andExpect(jsonPath("$[0].time.id").value(1L))
                .andExpect(jsonPath("$[0].time.startAt").value(MIA_RESERVATION_TIME.toString()))
                .andExpect(jsonPath("$[0].theme.name").value(WOOTECO_THEME_NAME))
                .andExpect(jsonPath("$[0].date").value(MIA_RESERVATION_DATE.toString()));
    }

    @Test
    @DisplayName("사용자, 테마, 예약 날짜로 예약 목록 검색 요청 시 상태코드 200을 반환한다.")
    void findReservationsByMemberIdAndThemeIdAndDateBetween() throws Exception {
        // given
        ReservationTime expectedTime = new ReservationTime(1L, MIA_RESERVATION_TIME);
        Reservation expectedReservation = MIA_RESERVATION(expectedTime, WOOTECO_THEME(), USER_MIA(), BOOKING);

        BDDMockito.given(bookingQueryService.findAllByMemberIdAndThemeIdAndDateBetween(anyLong(), anyLong(), any(), any()))
                .willReturn(List.of(expectedReservation));

        // when & then
        mockMvc.perform(get("/admin/reservations/searching")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("memberId", String.valueOf(1))
                        .param("themeId", String.valueOf(1))
                        .param("fromDate", MIA_RESERVATION_DATE.toString())
                        .param("toDate", MIA_RESERVATION_DATE.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberName").value(MIA_NAME))
                .andExpect(jsonPath("$[0].time.id").value(1L))
                .andExpect(jsonPath("$[0].time.startAt").value(MIA_RESERVATION_TIME.toString()))
                .andExpect(jsonPath("$[0].theme.name").value(WOOTECO_THEME_NAME))
                .andExpect(jsonPath("$[0].date").value(MIA_RESERVATION_DATE.toString()));
    }

    @Test
    @DisplayName("사용자, 테마, 예약 날짜로 예약 목록 검색 요청 시 검색 조건이 하나라도 없다면 상태코드 400을 반환한다.")
    void findReservationsByMemberIdAndThemeIdAndDateBetweenNotExistingAllParams() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/reservations/searching")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("memberId", String.valueOf(1))
                        .param("themeId", String.valueOf(1))
                        .param("toDate", MIA_RESERVATION_DATE.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("예약 DELETE 요청 시 상태코드 204를 반환한다.")
    void deleteReservation() throws Exception {
        // given
        BDDMockito.willDoNothing()
                .given(bookingScheduler)
                .delete(1L, STUBBED_LOGIN_MEMBER);

        // when & then
        mockMvc.perform(delete("/admin/reservations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
