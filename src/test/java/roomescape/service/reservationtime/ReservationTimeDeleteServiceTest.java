package roomescape.service.reservationtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
class ReservationTimeDeleteServiceTest {

    private ReservationTimeDeleteService reservationTimeDeleteService;

    @Autowired
    public ReservationTimeDeleteServiceTest(JdbcTemplate jdbcTemplate) {
        reservationTimeDeleteService = new ReservationTimeDeleteService(
                new ReservationTimeRepository(jdbcTemplate),
                new ReservationRepository(jdbcTemplate)
        );
    }

    @Test
    @DisplayName("예약 중이 아닌 시간을 삭제할 시 성공한다.")
    void deleteNotReservedTime_Success() {
        assertThatCode(() -> reservationTimeDeleteService.deleteReservationTime(2L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 예약 중인 시간을 삭제할 시 예외가 발생한다.")
    void deleteReservedTime_Failure() {
        assertThatThrownBy(() -> reservationTimeDeleteService.deleteReservationTime(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 예약중인 시간은 삭제할 수 없습니다.");
    }
}
