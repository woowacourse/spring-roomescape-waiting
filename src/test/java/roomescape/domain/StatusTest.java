package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatusTest {

    @DisplayName("예약이 취소된다")
    @Test
    void cancelStatus() {
        // given
        Status status = Status.statusWithoutId(LocalDateTime.of(2025, 1, 1, 10, 0, 0), ReservationStatus.RESERVED);

        // when
        status.cancelStatus();

        // then
        assertThat(status.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @DisplayName("예약된다")
    @Test
    void reserveStatus() {
        // given
        Status status = Status.statusWithoutId(LocalDateTime.of(2025, 1, 1, 10, 0, 0), ReservationStatus.WAITING);

        // when
        status.reserveStatus();

        // then
        assertThat(status.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }
}