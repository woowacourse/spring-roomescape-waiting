package roomescape.service.reservationtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeFindServiceTest {

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
