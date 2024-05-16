package roomescape.service.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.ReservationStatus;
import roomescape.service.BaseServiceTest;

class ReservationTimeFindServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeFindService reservationTimeFindService;

    @Test
    @DisplayName("날짜와 테마가 주어지면 각 시간의 예약 여부를 구한다.")
    void findAvailabilityByDateAndTheme() {
        LocalDate date = LocalDate.now().plusDays(1L);
        List<ReservationStatus> reservationStatuses = reservationTimeFindService.findIsBooked(date, 1L)
                .getReservationStatuses();
        Assertions.assertAll(
                () -> assertThat(reservationStatuses.size()).isEqualTo(2),
                () -> assertThat(reservationStatuses.get(0).isBooked()).isTrue(),
                () -> assertThat(reservationStatuses.get(1).isBooked()).isFalse()
        );
    }
}
