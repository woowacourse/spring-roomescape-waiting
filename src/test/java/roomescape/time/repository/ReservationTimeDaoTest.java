package roomescape.time.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.time.domain.ReservationTime;

import javax.sql.DataSource;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Sql(scripts = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("예약 시간 DAO")
class ReservationTimeDaoTest {

    private final ReservationTimeRepository reservationTimeRepository;

    @Autowired
    public ReservationTimeDaoTest(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.reservationTimeRepository = new ReservationTimeDao(jdbcTemplate, dataSource);
    }

    @DisplayName("예약 시간 DAO는 생성 요청이 들어오면 DB에 값을 저장한다.")
    @Test
    void save() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 10));

        // when
        ReservationTime newReservationTime = reservationTimeRepository.save(reservationTime);
        Optional<ReservationTime> actual = reservationTimeRepository.findById(newReservationTime.getId());

        // then
        assertThat(actual.isPresent()).isTrue();
    }

    @DisplayName("예약 시간 DAO는 조회 요청이 들어오면 id에 맞는 값을 반환한다.")
    @Test
    void findById() {
        // given
        Long id = 1L;

        // when
        Optional<ReservationTime> reservationTime = reservationTimeRepository.findById(id);

        // then
        assertThat(reservationTime.isPresent()).isTrue();
    }

    @DisplayName("예약 시간 DAO는 조회 요청이 들어오면 저장된 모든 값을 반환한다.")
    @Test
    void findAll() {
        // when
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        // then
        assertThat(reservationTimes.size()).isEqualTo(3);
    }

    @DisplayName("예약 시간 DAO는 삭제 요청이 들어오면 id에 맞는 값을 삭제한다.")
    @Test
    void deleteById() {
        // given
        Long id = 3L;

        // when
        reservationTimeRepository.deleteById(id);
        Optional<ReservationTime> actual = reservationTimeRepository.findById(id);

        // then
        assertThat(actual.isPresent()).isFalse();
    }

    @DisplayName("예약 시간 DAO는 중복 검증 요청이 들어오면 시작 시간에 맞는 값을 반환한다.")
    @Test
    void existsByStartAt() {
        // given
        LocalTime startAt = LocalTime.of(10, 00);

        // when
        Boolean actual = reservationTimeRepository.existsByStartAt(startAt);

        // then
        assertThat(actual).isTrue();
    }
}
