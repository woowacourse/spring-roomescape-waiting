package roomescape;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import roomescape.reservationwaiting.service.ReservationWaitingService;
import roomescape.theme.Theme;

@ExtendWith(SpringExtension.class)
class ReservationApplicationServiceTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private ReservationWaitingService reservationWaitingService;

    @Mock
    private WaitingPromotionService waitingPromotionService;

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
}