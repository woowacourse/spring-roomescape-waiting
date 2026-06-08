package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.domain.reservatinWaiting.ReservationWaiting;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;

public class ReservationWaitingTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "무서운 이야기", "공포", "example.com");

    @Test
    void 예약_대기가_제대로_생성되었는지_확인한다() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(1L, "브라운", new ReservationSlot(LocalDate.now(), reservationTime,
                theme),
                LocalDateTime.now());

        assertThat(reservationWaiting.getId()).isEqualTo(1L);
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 미래_날짜_시간으로_예약_대기하면_정상_작동한다(int day, int time) {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().plusHours(time));

        ReservationWaiting reservationWaiting = new ReservationWaiting(1L, "브라운", new ReservationSlot(LocalDate.now().plusDays(day), reservationTime,
                theme),
                LocalDateTime.now());

        assertThatCode(reservationWaiting::validatePastDateTime).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 과거_날짜_시간으로_예약_대기하면_예외가_발생한다(int day, int time) {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().minusHours(time));

        ReservationWaiting reservationWaiting = new ReservationWaiting(1L, "브라운", new ReservationSlot(LocalDate.now().minusDays(day), reservationTime,
                theme),
                LocalDateTime.now());

        assertThatThrownBy(reservationWaiting::validatePastDateTime)
                .isExactlyInstanceOf(ExpiredDateTimeException.class);

    }
}
