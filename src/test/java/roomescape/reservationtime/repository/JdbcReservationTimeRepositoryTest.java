package roomescape.reservationtime.repository;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.test_config.fixture.SqlFixtureGenerator;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({JdbcReservationTimeRepository.class, SqlFixtureGenerator.class})
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SqlFixtureGenerator sqlFixtureGenerator;



    @Test
    @DisplayName("예약 시간을 저장한다.")
    void save() {

        // given
        ReservationTime reservationTime = ReservationTime.create(LocalTime.of(10, 0));

        // given
        ReservationTime saved = reservationTimeRepository.save(reservationTime);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("삭제된 예약 시간은 id로 조회되지 않는다.")
    void findById_softDelete() {
        // given
        ReservationTime reservationTime = sqlFixtureGenerator.insertDeletedReservationTime(LocalTime.of(10, 0));

        // when
        Optional<ReservationTime> found = reservationTimeRepository.findById(reservationTime.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 시간 목록을 조회한다")
    void findAll() {
        sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        assertThat(reservationTimes).hasSize(1);
        assertThat(reservationTimes.getFirst().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("예약 시간 목록은 삭제되지 않은 예약 시간만 조회한다.")
    void findAll_softDelete() {
        // given
        sqlFixtureGenerator.insertDeletedReservationTime(LocalTime.of(10, 0));
        ReservationTime activeTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(12, 0));

        // when
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        // then
        assertThat(reservationTimes)
                .extracting(ReservationTime::getId, ReservationTime::getStartAt)
                .containsExactly(Tuple.tuple(activeTime.getId(), activeTime.getStartAt()));
    }

    @Test
    void 예약_시간_존재_여부를_조회한다() {
        sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));

        boolean exists = reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0));

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("삭제된 예약 시간은 존재하지 않는 것으로 조회한다.")
    void existsByStartAt_softDelete() {
        // given
        sqlFixtureGenerator.insertDeletedReservationTime(LocalTime.of(10, 0));

        // when
        boolean exists = reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0));

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("예약 시간을 삭제한다.")
    void cancelById_success_softDelete() {
        // given
        ReservationTime reservationTime = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 10, 0);

        // when
        boolean deleted = reservationTimeRepository.cancelById(reservationTime.getId(), now);

        // then
        assertThat(deleted).isTrue();
        assertThat(reservationTimeRepository.findAll()).isEmpty();

        Map<String, Object> deleteInfo = findDeleteInfoById(reservationTime.getId());
        assertThat(((Timestamp) deleteInfo.get("deleted_at")).toLocalDateTime()).isEqualTo(now);
        assertThat(((Number) deleteInfo.get("delete_token")).longValue()).isEqualTo(reservationTime.getId());
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간은 삭제되지 않는다.")
    void cancelById_fail_notFound() {
        // given
        Long id = 1L;

        // when
        boolean deleted = reservationTimeRepository.cancelById(id, LocalDateTime.now());

        // then
        assertThat(deleted).isFalse();
    }

    private Map<String, Object> findDeleteInfoById(Long id) {
        return jdbcTemplate.queryForMap("""
                SELECT deleted_at, delete_token
                FROM reservation_time
                WHERE id = :id
                """, new MapSqlParameterSource("id", id));
    }
}
