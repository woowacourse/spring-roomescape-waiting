package roomescape.time.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidArgumentException;
import roomescape.reservation.time.domain.ReservationTimeId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationTimeIdTest {

    @Test
    @DisplayName("예약 시간 ID가 null이면 예외가 발생한다")
    void validateNullReservationTimeId() {
        // when
        // then
        assertThatThrownBy(() -> ReservationTimeId.from(null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking null]: DomainId.value");
    }

    @Test
    @DisplayName("유효한 ID로 ReservationTimeId 객체를 생성할 수 있다")
    void createValidReservationTimeId() {
        // given
        final Long id = 1L;

        // when
        final ReservationTimeId reservationTimeId = ReservationTimeId.from(id);

        // then
        assertAll(() -> {
            assertThat(reservationTimeId).isNotNull();
            assertThat(reservationTimeId.getValue()).isEqualTo(id);
        });
    }
} 
