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
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;

public class ReservationWaitingTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "무서운 이야기", "공포", "example.com");

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 미래_날짜_시간으로_예약_대기를_생성하면_정상_작동한다(int day, int hour) {
        LocalDate futureDate = LocalDate.now().plusDays(day);
        ReservationTime futureTime = new ReservationTime(1L, LocalTime.now().plusHours(hour));

        assertThatCode(() -> ReservationWaiting.create("브라운", futureDate, futureTime, theme))
                .doesNotThrowAnyException();
    }

    @Test
    void create로_생성된_예약_대기는_id와_sequence가_null이다() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime futureTime = new ReservationTime(1L, LocalTime.now().plusHours(1));

        ReservationWaiting waiting = ReservationWaiting.create("브라운", futureDate, futureTime, theme);

        assertThat(waiting.getId()).isNull();
        assertThat(waiting.getSequence()).isNull();
        assertThat(waiting.getName()).isEqualTo("브라운");
        assertThat(waiting.getDate()).isEqualTo(futureDate);
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 과거_날짜_시간으로_예약_대기를_생성하면_예외가_발생한다(int day, int hour) {
        LocalDate pastDate = LocalDate.now().minusDays(day);
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.now().minusHours(hour));

        assertThatThrownBy(() -> ReservationWaiting.create("브라운", pastDate, pastTime, theme))
                .isExactlyInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void restore는_과거_날짜_시간이어도_예외_없이_복원된다() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.parse("10:00"));

        assertThatCode(() -> ReservationWaiting.restore(1L, "브라운", pastDate, pastTime, theme, 1L, LocalDateTime.now()))
                .doesNotThrowAnyException();
    }

    @Test
    void restore로_생성된_예약_대기의_필드가_올바르게_설정된다() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime createdAt = LocalDateTime.now();

        ReservationWaiting waiting = ReservationWaiting.restore(1L, "브라운", date, reservationTime, theme, 2L, createdAt);

        assertThat(waiting.getId()).isEqualTo(1L);
        assertThat(waiting.getName()).isEqualTo("브라운");
        assertThat(waiting.getDate()).isEqualTo(date);
        assertThat(waiting.getTime()).isEqualTo(reservationTime);
        assertThat(waiting.getTheme()).isEqualTo(theme);
        assertThat(waiting.getSequence()).isEqualTo(2L);
        assertThat(waiting.getCreatedAt()).isEqualTo(createdAt);
    }
}
