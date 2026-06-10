package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.custom.InvalidDomainValueException;

public class WaitTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void nameBlankExceptionTest(String name) {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), name,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme)))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void createdAtNullExceptionTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Wait(1L, null, "fizz", new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme)))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void slotNullExceptionTest() {
        assertThatThrownBy(() -> new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", null))
                .isInstanceOf(InvalidDomainValueException.class);
    }
}
