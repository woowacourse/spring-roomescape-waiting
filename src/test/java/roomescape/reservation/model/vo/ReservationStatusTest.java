package roomescape.reservation.model.vo;

import java.time.LocalDateTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationStatusTest {

    @Test
    @DisplayName("예약 시간이 now 이전이라면 ENDED, 이후면 CONFIRMED이다.")
    void testReservationStatus_confirmed() {
        LocalDateTime beforeStandard = LocalDateTime.of(2025, 5, 5, 13, 0);
        LocalDateTime standardDateTime = LocalDateTime.of(2025, 5, 10, 13, 0);
        LocalDateTime afterStandard = LocalDateTime.of(2025, 5, 15, 13, 0);
        SoftAssertions.assertSoftly(
            softAssertions -> {
                softAssertions.assertThat(
                        ReservationStatus.getStatus(beforeStandard, standardDateTime))
                    .isEqualTo(ReservationStatus.ENDED);
                softAssertions.assertThat(
                        ReservationStatus.getStatus(afterStandard, standardDateTime))
                    .isEqualTo(ReservationStatus.CONFIRMED);
            }
        );
    }
}
