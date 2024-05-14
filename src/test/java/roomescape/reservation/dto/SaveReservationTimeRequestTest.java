package roomescape.reservation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.dto.SaveReservationTimeRequest;
import roomescape.reservation.model.ReservationTime;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class SaveReservationTimeRequestTest {

    @DisplayName("SaveReservationTimeRequest를 ReservationTime으로 변환한다.")
    @Test
    void toReservationTimeTest() {
        // Given
        final LocalTime startAt = LocalTime.of(1, 12);
        final SaveReservationTimeRequest request = new SaveReservationTimeRequest(startAt);

        // When
        final ReservationTime reservationTime = request.toReservationTime();

        // Then
        assertThat(reservationTime).isNotNull();
    }
}
