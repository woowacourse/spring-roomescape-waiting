package roomescape.reservation.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.model.ValidateException;

class ReservationOrderTest {

    @Test
    @DisplayName("예약 순서가 null이면 예외를 발생한다.")
    void failCreateReservationOrderByNull() {
        Assertions.assertThatThrownBy(() -> new ReservationOrder(null))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("예약 순서가 음수이면 예외를 발생한다.")
    void failCreateReservationOrderByNegativeNumber() {
        Assertions.assertThatThrownBy(() -> new ReservationOrder(-1L))
                .isInstanceOf(ValidateException.class);
    }
}
