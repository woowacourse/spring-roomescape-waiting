package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.custom.InvalidDomainValueException;

public class SlotTest {

    @Test
    void dateNullExceptionTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Slot(null, reservationTime, theme))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void reservationTimeNullExceptionTest() {
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Slot(LocalDate.of(2026, 5, 2), null, theme))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void themeNullExceptionTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        assertThatThrownBy(() -> new Slot(LocalDate.of(2026, 5, 2), reservationTime, null))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void isPastTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(11, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");

        LocalDateTime now = LocalDateTime.of(2026, 5, 2, 10, 0);
        Slot slot = new Slot(LocalDate.of(2026, 5, 1), reservationTime, theme);

        assertThat(slot.isPast(now)).isEqualTo(true);
    }
}
