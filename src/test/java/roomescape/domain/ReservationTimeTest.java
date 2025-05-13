package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.domain.reservation.ReservationTime;

class ReservationTimeTest {

    @ParameterizedTest
    @CsvSource({"12:00", "22:00"})
    void 예약_시간은_12시_부터_22시_까지_가능하다_성공(LocalTime time) {
        assertThatCode(() -> new ReservationTime(time))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({"11:59", "22:01"})
    void 예약_시간은_12시_부터_22시_까지_가능하다_실패(LocalTime time) {
        assertThatThrownBy(() -> new ReservationTime(time))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("해당 시간은 예약 가능 시간이 아닙니다.");
    }
}