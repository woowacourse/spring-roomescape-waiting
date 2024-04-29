package roomescape.repository;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.reservation.ReservationTime;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private final String rawTime1 = "10:00";
    private final ReservationTime reservationTime1 = new ReservationTime(rawTime1);

    private final String rawTime2 = "11:00";
    private final ReservationTime reservationTime2 = new ReservationTime(rawTime2);

    @DisplayName("예약 시간 정보를 DB에 저장한다.")
    @Test
    void save() {
        ReservationTime saved = reservationTimeRepository.save(reservationTime1);
        assertThat(saved).isEqualTo(new ReservationTime(saved.getId(), rawTime1));
    }

    @DisplayName("id값을 통해 예약 시간 정보를 DB에서 조회한다.")
    @Test
    void findById() {
        ReservationTime saved = reservationTimeRepository.save(reservationTime1);

        assertThat(reservationTimeRepository.findById(saved.getId())).isEqualTo(saved);
    }

    @DisplayName("모든 예약 시간 정보를 DB에서 조회한다.")
    @Test
    void findAll() {
        ReservationTime saved1 = reservationTimeRepository.save(reservationTime1);
        ReservationTime saved2 = reservationTimeRepository.save(reservationTime2);

        assertThat(reservationTimeRepository.findAll()).containsExactly(saved1, saved2);
    }

    @DisplayName("id값을 통해 예약 시간 정보를 DB에서 삭제한다.")
    @Test
    void deleteById() {
        ReservationTime saved1 = reservationTimeRepository.save(reservationTime1);
        ReservationTime saved2 = reservationTimeRepository.save(reservationTime2);

        reservationTimeRepository.deleteById(saved1.getId());

        assertThat(reservationTimeRepository.findAll()).containsExactly(saved2);
    }
}
