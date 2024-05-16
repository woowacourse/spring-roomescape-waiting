package roomescape.service.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.service.BaseServiceTest;

class ReservationTimeFindServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeFindService reservationTimeFindService;

    @Test
    @DisplayName("날짜와 테마가 주어지면 각 시간의 예약 여부를 구한다.")
    void findAvailabilityByDateAndTheme() {
        LocalDate date = LocalDate.now().plusDays(1L);
        ReservationStatus reservationStatus = reservationTimeFindService.findIsBooked(date, 1L);
        assertThat(reservationStatus.getReservationStatus())
                .isEqualTo(Map.of(
                        new ReservationTime(1L, LocalTime.of(10, 0)), true,
                        new ReservationTime(2L, LocalTime.of(11, 0)), false
                ));
    }
}
