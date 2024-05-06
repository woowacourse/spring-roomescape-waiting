package roomescape.infrastructure.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.TimeSlot;

@JdbcTest
@Import(JdbcReservationTimeRepository.class)
class JdbcReservationTimeRepositoryTest {
    private static final String INSERT_SQL = "insert into reservation_time (id, start_at) values (?, ?)";

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("id를 검색해서 존재하면 예약을 반환한다.")
    void shouldReturnOptionalReservationTimeWhenFindById() {
        jdbcTemplate.update(INSERT_SQL, 1L, LocalTime.of(10, 0));
        Optional<ReservationTime> reservationTime = timeRepository.findById(1L);
        assertThat(reservationTime).isPresent();
    }

    @Test
    @DisplayName("id를 검색해서 존재하지 않으면 빈 옵셔널 객체를 반환한다.")
    void shouldReturnOptionalEmptyWhenReservationTimeDoesNotExist() {
        Optional<ReservationTime> reservationTime = timeRepository.findById(1L);
        assertThat(reservationTime).isEmpty();
    }

    @Test
    @DisplayName("id가 생성되지 않은 예약 시간을 추가하면, id가 주어진 예약 시간이 저장된다.")
    void shouldReturnReservationTimeWithIdWhenCreateReservationTimeWithoutId() {
        ReservationTime reservationTimeWithoutId = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime reservationTime = timeRepository.create(reservationTimeWithoutId);
        int totalRowCount = getTotalRowCount();
        assertAll(
                () -> assertThat(reservationTime.getId()).isNotNull(),
                () -> assertThat(totalRowCount).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("특정 시간이 저장소에 존재하면 true를 반환한다.")
    void shouldReturnTrueWhenReservationTimeAlreadyExist() {
        LocalTime startAt = LocalTime.of(10, 0);
        jdbcTemplate.update(INSERT_SQL, 1L, startAt);
        boolean exists = timeRepository.existsByStartAt(startAt);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("예약 시간을 모두 조회한다.")
    void shouldReturnReservationsWhenFindAll() {
        jdbcTemplate.update(INSERT_SQL, 1L, LocalTime.of(10, 0));
        List<ReservationTime> reservationTimes = timeRepository.findAll();
        assertThat(reservationTimes).hasSize(1);
    }

    @Test
    @DisplayName("특정 시간이 저장소에 존재하지 않으면 false를 반환한다.")
    void shouldReturnFalseWhenReservationTimeAlreadyExist() {
        LocalTime startAt = LocalTime.of(10, 0);
        boolean exists = timeRepository.existsByStartAt(startAt);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("id로 예약 시간을 삭제한다.")
    void shouldDeleteTimeById() {
        jdbcTemplate.update(INSERT_SQL, 1L, LocalTime.of(10, 0));
        timeRepository.deleteById(1L);
        int count = getTotalRowCount();
        assertThat(count).isZero();
    }

    @Test
    @Sql("/insert-reservations.sql")
    @DisplayName("날짜와 테마 id가 주어지면, 예약 가능한 시간을 반환한다.")
    void shouldReturnAvailableTimes() {
        LocalDate date = LocalDate.of(2024, 12, 25);
        List<TimeSlot> times = timeRepository.getReservationTimeAvailabilities(date, 1L)
                .stream()
                .filter(time -> !time.isBooked())
                .toList();
        assertThat(times).hasSize(4);
    }

    private int getTotalRowCount() {
        String sql = "select count(*) from reservation_time";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
