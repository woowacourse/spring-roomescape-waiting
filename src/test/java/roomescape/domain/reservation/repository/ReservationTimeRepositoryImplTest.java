package roomescape.domain.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.RepositoryTest;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;

class ReservationTimeRepositoryImplTest extends RepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new ReservationTimeRepositoryImpl(jdbcTemplate);
        jdbcTemplate.update("insert into reservation_time (start_at) values('10:00')");
    }

    @AfterEach
    void setDown() {
        jdbcTemplate.update("delete from reservation_time");
    }

    @DisplayName("예약시간 모두를 불러옵니다.")
    @Test
    void should_find_all() {
        int expectedSize = 1;

        int actualSize = reservationTimeRepository.findAll().size();

        assertThat(actualSize).isEqualTo(expectedSize);
    }

    @DisplayName("원하는 ID의 예약시간을 불러옵니다.")
    @Test
    void should_find_reservation_time_by_id() {
        ReservationTime expectedReservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

        ReservationTime actualReservationTime = reservationTimeRepository.findById(1L).get();

        assertThat(actualReservationTime).isEqualTo(expectedReservationTime);
    }

    @DisplayName("동일한 시간의 예약시간이 존재하는지 확인할 수 있습니다.")
    @Test
    void should_return_true_when_same_time_is_already_exist() {
        assertThat(reservationTimeRepository.existByStartAt(LocalTime.of(10, 0))).isTrue();
    }

    @DisplayName("동일한 시간의 예약시간이 존재하지 않는 경우를 확인할 수 있습니다")
    @Test
    void should_return_false_when_same_time_is_no_exist() {
        assertThat(reservationTimeRepository.existByStartAt(LocalTime.of(11, 0))).isFalse();
    }

    @DisplayName("예약시간을 추가할 수 있습니다.")
    @Test
    void should_get_reservation_time_after_insert() {
        ReservationTime requestReservationTime = new ReservationTime(null, LocalTime.of(11, 0));
        ReservationTime actualReservationTime = reservationTimeRepository.insert(requestReservationTime);

        assertThat(actualReservationTime.getId()).isNotNull();
    }

    @DisplayName("원하는 ID의 예약시간을 삭제할 수 있습니다.")
    @Test
    void should_deleteById() {
        int expectedCount = 0;

        reservationTimeRepository.deleteById(1L);
        int actualCount = jdbcTemplate.queryForObject("select count(*) from reservation_time where id = 1",
                Integer.class);

        assertThat(actualCount).isEqualTo(expectedCount);
    }
}
