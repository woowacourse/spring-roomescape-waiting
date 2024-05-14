package roomescape.reservation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTimeTest {

    @DisplayName("인덱스를 입력하면 해당 아이디를 가진 ReservationTime 객체를 생성해서 반환한다.")
    @Test
    void initializeIndex() {
        // Given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(1, 12));
        final Long initialIndex = 3L;

        // When
        final ReservationTime initIndexReservationTime = reservationTime.initializeIndex(initialIndex);

        // Then
        assertThat(initIndexReservationTime.getId()).isEqualTo(initialIndex);
    }

    @DisplayName("예약 시간에 공백을 입력하면 예외를 발생한다.")
    @Test
    void throwExceptionWhenReservationTimeBlank() {
        // When & Then
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시간 정보는 공백을 입력할 수 없습니다.");
    }
}
