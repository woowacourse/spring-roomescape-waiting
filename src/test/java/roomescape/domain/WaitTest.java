package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.custom.InvalidDomainValueException;

public class WaitTest {

    @Test
    void memberNullExceptionTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), null,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme)))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void createdAtNullExceptionTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Wait(1L, null, new Member(1L, "fizz"), new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme)))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void slotNullExceptionTest() {
        assertThatThrownBy(() -> new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), new Member(1L, "fizz"), null))
                .isInstanceOf(InvalidDomainValueException.class);
    }
}
