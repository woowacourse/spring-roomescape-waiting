package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;

@JdbcTest
public class JdbcReservationTimeRepositoryTest {


    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @AfterEach
    void afterEach() {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
        List<String> tableNames = jdbcTemplate.queryForList(sql, String.class);

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String tableName : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1");
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    void saveTest() {
        ReservationTime reservationTimeWithoutId = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(reservationTimeWithoutId);

        assertThat(reservationTime.getId()).isEqualTo(1L);
    }

    @Test
    void findByIdTest() {
        String sql = "INSERT INTO `reservation_time` (`start_at`) VALUES (?)";
        jdbcTemplate.update(sql, "10:00");

        Optional<ReservationTime> reservationTime = reservationTimeRepository.findById(1L);

        assertThat(reservationTime.orElseThrow().getId()).isEqualTo(1L);
    }

    @Test
    void findAllTest() {
        String sql = "INSERT INTO `reservation_time` (`start_at`) VALUES (?)";
        jdbcTemplate.update(sql, "10:00");
        jdbcTemplate.update(sql, "11:00");

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        assertThat(reservationTimes.size()).isEqualTo(2);
    }

    @Test
    void findReservedTimesByDateAndThemeIdTest() {
        String insertReservationTimeSql = "INSERT INTO `reservation_time` (`start_at`) VALUES (?)";
        jdbcTemplate.update(insertReservationTimeSql, "10:00");
        jdbcTemplate.update(insertReservationTimeSql, "11:00");
        jdbcTemplate.update(insertReservationTimeSql, "12:00");

        String insertThemeSql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertThemeSql, "방탈출1", "방탈출1 설명", "url.jpg");

        String insertReservationSql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(insertReservationSql, "fizz", "2026-05-02", 1L, 1L);
        jdbcTemplate.update(insertReservationSql, "fizz", "2026-05-02", 2L, 1L);

        List<ReservationTime> reservedTimes = reservationTimeRepository.findReservedTimesByDateAndTheme_Id(
                LocalDate.of(2026, 5, 2),
                1L);

        assertThat(reservedTimes.get(0)).isEqualTo(new ReservationTime(1L, LocalTime.of(10, 0)));
        assertThat(reservedTimes.get(1)).isEqualTo(new ReservationTime(2L, LocalTime.of(11, 0)));
    }

    @Test
    void deleteTest() {
        String insertReservationTimeSql = "INSERT INTO `reservation_time` (`start_at`) VALUES (?)";
        jdbcTemplate.update(insertReservationTimeSql, "10:00");

        reservationTimeRepository.delete(1L);

        String readAllReservationTimeCountSql = "SELECT COUNT(*) FROM `reservation_time`";
        int count = jdbcTemplate.queryForObject(readAllReservationTimeCountSql, Integer.class);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void existsByStartAt() {
        String insertReservationTimeSql = "INSERT INTO `reservation_time` (`start_at`) VALUES (?)";
        jdbcTemplate.update(insertReservationTimeSql, "10:00");

        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).isTrue();
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(11, 0))).isFalse();
    }
}
