package roomescape.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;
import roomescape.dto.ReservationWaitingRequest;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.facade.ReservationFacade;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ReservationWaitingService;
import roomescape.service.ThemeService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservationFacadeMockTest {

    private static final LocalDate DATE = LocalDate.of(2026, 8, 5);
    private static final LocalDate PAST_DATE = LocalDate.of(2020, 1, 1);
    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 1L;
    private static final ReservationTime TIME = new ReservationTime(TIME_ID, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(THEME_ID, "공포", "무서운 테마", "https://example.com/horror.jpg");

    @Mock
    private ReservationService reservationService;

    @Mock
    private ReservationTimeService reservationTimeService;

    @Mock
    private ReservationWaitingService reservationWaitingService;

    @Mock
    private ThemeService themeService;

    @InjectMocks
    private ReservationFacade facade;

    @Test
    void 이미_대기를_신청한_예약에는_다시_신청할_수_없다() {
        Reservation other = new Reservation(99L, "티뉴", DATE, TIME, THEME);
        given(reservationTimeService.findById(TIME_ID)).willReturn(TIME);
        given(themeService.findById(THEME_ID)).willReturn(THEME);
        given(reservationService.findByDateAndThemeId(DATE, THEME_ID))
                .willReturn(new Reservations(List.of(other)));
        given(reservationWaitingService.existBy("민욱", 99L)).willReturn(true);

        ReservationWaitingRequest request = new ReservationWaitingRequest("민욱", DATE, TIME_ID, THEME_ID);

        assertThatThrownBy(() -> facade.addWaiting(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("이미 대기");
    }
}
