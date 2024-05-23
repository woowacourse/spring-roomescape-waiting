package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.IllegalRequestException;
import roomescape.time.fixture.DateTimeFixture;

class ReservationDateTest {

    @DisplayName("날짜가 null인 경우 생성 시 예외가 발생한다")
    @Test
    void should_throw_exception_when_value_is_null() {
        assertThatThrownBy(() -> new ReservationDate(null))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("예약 날짜가 과거인 경우 예외가 발생한다")
    @Test
    void should_throw_exception_when_reservation_date_is_past() {
        assertThatThrownBy(() -> new ReservationDate(DateTimeFixture.YESTERDAY))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("정상적인 날짜로 생성 시 예외가 발생하지 않는다")
    @Test
    void should_create_reservation_date_when_with_valid_value() {
        assertThatCode(() -> new ReservationDate(DateTimeFixture.DAY_AFTER_TOMORROW))
                .doesNotThrowAnyException();
    }
}
