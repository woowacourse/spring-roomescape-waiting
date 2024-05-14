package roomescape.reservation.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
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
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.AdminReservationSaveRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixture.*;

@WebMvcTest(
        value = AdminReservationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebMvcConfiguration.class, LoginMemberArgumentResolver.class, AdminAuthorizationInterceptor.class})
)
class AdminReservationControllerTest extends ControllerTest {
    @MockBean
    private ReservationService reservationService;

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
        Reservation expectedReservation = MIA_RESERVATION(expectedTime, expectedTheme, USER_MIA(1L));

        BDDMockito.given(reservationTimeService.findById(anyLong()))
                .willReturn(expectedTime);
        BDDMockito.given(themeService.findById(anyLong()))
                .willReturn(expectedTheme);
        BDDMockito.given(memberService.findById(anyLong()))
                .willReturn(USER_MIA(1L));
        BDDMockito.given(reservationService.create(any()))
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
}
