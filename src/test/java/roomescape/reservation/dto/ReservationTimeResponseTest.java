package roomescape.reservation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.model.ReservationTime;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationTimeResponseTest {

    @DisplayName("ReservationTime을 입력받으면 ReservationTimeResponse로 변환한다.")
    @Test
    void convertDtoTest() {
        // Given
        final LocalTime startAt = LocalTime.of(1, 12);
        final ReservationTime reservationTime = new ReservationTime(1L, startAt);

        // When
        final ReservationTimeResponse reservationTimeResponse = ReservationTimeResponse.from(reservationTime);

        // Then
        assertAll(
                () -> assertThat(reservationTimeResponse).isNotNull(),
                () -> assertThat(reservationTimeResponse.id()).isEqualTo(reservationTime.getId()),
                () -> assertThat(reservationTimeResponse.startAt()).isEqualTo(reservationTime.getStartAt()
                ));
    }
}
