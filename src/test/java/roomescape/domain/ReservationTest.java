package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.custom.InvalidDomainValueException;

public class ReservationTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void nameBlankExceptionTest(String name) {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme("피즈의 모험", "모험 이야기", "url.jpg");
        assertThatThrownBy(() -> new Reservation(1L, name, new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme)))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void slotNullExceptionTest() {
        assertThatThrownBy(() -> new Reservation(1L, "fizz", null))
                .isInstanceOf(InvalidDomainValueException.class);
    }
}
