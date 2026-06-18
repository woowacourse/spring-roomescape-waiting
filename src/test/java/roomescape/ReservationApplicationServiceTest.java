package roomescape;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import roomescape.application.ReservationApplicationService;
import roomescape.application.WaitingPromotionService;
import roomescape.exception.ConflictException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.Reservation;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.reservationwaiting.ReservationWaiting;
import roomescape.reservationwaiting.service.ReservationWaitingService;
import roomescape.theme.Theme;
import roomescape.theme.service.ThemeService;

@ExtendWith(SpringExtension.class)
class ReservationApplicationServiceTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private ReservationWaitingService reservationWaitingService;

    @Mock
    private WaitingPromotionService waitingPromotionService;

    @Mock
    private ThemeService themeService;

    @Mock
    private ReservationTimeService reservationTimeService;

    @InjectMocks
    private ReservationApplicationService reservationApplicationService;

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(1);
    private static final Theme THEME = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
    private static final ReservationTime TIME = ReservationTime.of(1L, LocalTime.parse("10:00"));
    private static final Reservation RESERVATION = Reservation.of(1L, "도우너", FUTURE_DATE, THEME, TIME);

    @Test
    void 예약자와_대기_요청자가_동일하면_테스트_실패() {
        // given
        when(reservationService.findByDateAndThemeIdAndTimeId(FUTURE_DATE, 1L, 1L))
                .thenReturn(RESERVATION);

        // when & then
        assertThrows(ConflictException.class,
                () -> reservationApplicationService.saveWaiting("도우너", FUTURE_DATE, 1L, 1L));
    }

    @Test
    void 예약이_존재하지_않는_슬롯에_대기_요청시_실패() {
        // given
        when(reservationService.findByDateAndThemeIdAndTimeId(FUTURE_DATE, 11L, 5L))
                .thenThrow(ResourceNotFoundException.class);

        // when & then
        assertThrows(ResourceNotFoundException.class,
                () -> reservationApplicationService.saveWaiting("도우너", FUTURE_DATE, 11L, 5L));
    }

    @Test
    void 예약_대기를_저장한다() {
        // given
        Theme theme = THEME;
        ReservationTime time = TIME;
        ReservationWaiting savedWaiting = ReservationWaiting.createNew(
                FUTURE_DATE,
                theme,
                time,
                "둘리",
                LocalDateTime.parse("2026-06-18T12:00:00")
        ).withId(1L);

        when(reservationService.findByDateAndThemeIdAndTimeId(FUTURE_DATE, 1L, 1L))
                .thenReturn(RESERVATION);
        when(themeService.getById(1L)).thenReturn(theme);
        when(reservationTimeService.getById(1L)).thenReturn(time);
        when(reservationWaitingService.save("둘리", FUTURE_DATE, theme, time)).thenReturn(savedWaiting);

        // when
        ReservationWaiting result = reservationApplicationService.saveWaiting("둘리", FUTURE_DATE, 1L, 1L);

        // then
        assertThat(result).isEqualTo(savedWaiting);
        verify(reservationWaitingService).save("둘리", FUTURE_DATE, theme, time);
    }
}
