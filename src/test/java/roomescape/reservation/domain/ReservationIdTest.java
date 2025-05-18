package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidArgumentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationIdTest {

    @Test
    @DisplayName("예약 ID가 null이면 예외가 발생한다")
    void validateNullReservationId() {
        // when
        // then
        assertThatThrownBy(() -> ReservationId.from(null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking null]: DomainId.value");
    }

    @Test
    @DisplayName("유효한 ID로 ReservationId 객체를 생성할 수 있다")
    void createValidReservationId() {
        // given
        final Long id = 1L;

        // when
        final ReservationId reservationId = ReservationId.from(id);

        // then
        assertAll(() -> {
            assertThat(reservationId).isNotNull();
            assertThat(reservationId.getValue()).isEqualTo(id);
        });
    }
} 
