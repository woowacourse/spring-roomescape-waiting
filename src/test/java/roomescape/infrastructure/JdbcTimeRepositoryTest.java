package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.testFixture.Fixture.resetH2TableIds;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.ReservationTime;
import roomescape.infrastructure.jdbc.JdbcTimeRepository;

@JdbcTest
@Import(JdbcTimeRepository.class)
@ActiveProfiles("test")
class JdbcTimeRepositoryTest {

    @Autowired
    private JdbcTimeRepository timeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        resetH2TableIds(jdbcTemplate);
    }

    @DisplayName("save 후 생성된 id를 반환한다.")
    @Test
    void saveTest() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        ReservationTime reservationTime = ReservationTime.of(1L, startAt);

        // when
        Long savedId = timeRepository.save(reservationTime);

        // then
        String sql = "SELECT * FROM reservation_time WHERE id = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, savedId);

        assertAll(
                () -> assertThat(savedId).isNotNull(),
                () -> assertThat(result).containsEntry("start_at", Time.valueOf(startAt))
        );
    }

    @DisplayName("모든 시간을 조회할 수 있다.")
    @Test
    void findAllTest() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('11:00:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('12:00:00')");

        // when
        List<ReservationTime> times = timeRepository.findAll();

        // then
        assertThat(times).hasSize(3);
        assertThat(times)
                .extracting(ReservationTime::getStartAt)
                .containsExactly(
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        LocalTime.of(12, 0)
                );
    }

    @DisplayName("id로 예약을 삭제할 수 있다.")
    @Test
    void deleteByIdTest() {
        // given
        assertThat(timeRepository.findAll()).isEmpty();
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00:00')");
        assertThat(timeRepository.findAll()).hasSize(1);

        // when
        timeRepository.deleteById(1L);

        // then
        assertThat(timeRepository.findAll()).isEmpty();
    }

    @DisplayName("다른 테이블에서 참조되고 있는 시간 삭제 시 예외 발생")
    @Test
    void error_when_delete_referencedTime() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1L, '10:00:00')");
        jdbcTemplate.update(
                "INSERT INTO reservation (date ,time_id) VALUES ('2025-01-01', 1L)");

        // when & then
        assertThatThrownBy(() -> timeRepository.deleteById(1L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
