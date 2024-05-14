package roomescape.service.reservationtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.request.ReservationTimeSaveRequest;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
class ReservationTimeCreateServiceTest {

    private ReservationTimeCreateService reservationTimeCreateService;

    @Autowired
    public ReservationTimeCreateServiceTest(JdbcTemplate jdbcTemplate) {
        reservationTimeCreateService = new ReservationTimeCreateService(
                new ReservationTimeRepository(jdbcTemplate)
        );
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간인 경우 성공한다")
    void checkDuplicateTime_Success() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(12, 0));

        assertThatCode(() -> reservationTimeCreateService.createReservationTime(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 존재하는 예약 시간인 경우 예외가 발생한다.")
    void checkDuplicateTime_Failure() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(11, 0));

        assertThatThrownBy(() -> reservationTimeCreateService.createReservationTime(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 예약 시간입니다.");
    }
}
