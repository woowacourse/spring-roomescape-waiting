package roomescape.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@JdbcTest
public class JdbcReservationRepositoryTest {

    private ReservationRepository reservationRepository;

    private ReservationTime reservationTime;

    private Theme theme;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        reservationRepository = new JdbcReservationRepository(jdbcTemplate);

        String insertReservationTimeSql = "INSERT INTO `reservation_time` (`start_at`) VALUES (?)";
        jdbcTemplate.update(insertReservationTimeSql, "10:00");
        jdbcTemplate.update(insertReservationTimeSql, "11:00");
        jdbcTemplate.update(insertReservationTimeSql, "12:00");

        String insertThemeSql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertThemeSql, "방탈출1", "방탈출1 설명", "url.jpg");

        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "방탈출1", "방탈출1 설명", "url.jpg");
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
        Reservation reservationWithoutId = new Reservation("fizz",
                new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme));

        Reservation reservation = reservationRepository.save(reservationWithoutId);

        assertThat(reservation.getId()).isEqualTo(1L);
    }

    @Test
    void findByIdTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        Reservation reservation = reservationRepository.findById(1L).get();

        assertThat(reservation.getName()).isEqualTo("fizz");
        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2026, 5, 2));
        assertThat(reservation.getTime().getId()).isEqualTo(1L);
        assertThat(reservation.getTheme().getId()).isEqualTo(1L);
    }

    @Test
    void findByNameTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);
        jdbcTemplate.update(sql, "tree", "2026-05-02", 2L, 1L);
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 3L, 1L);

        List<Reservation> reservations = reservationRepository.findByName("fizz");

        assertThat(reservations.size()).isEqualTo(2);
        assertThat(reservations.get(0).getName()).isEqualTo("fizz");
        assertThat(reservations.get(1).getName()).isEqualTo("fizz");

        assertThat(reservationRepository.findByName("user").size()).isEqualTo(0);
    }

    @Test
    void findAllTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 2L, 1L);

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(2);
    }

    @Test
    void deleteByIdTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        reservationRepository.deleteById(1L);

        String findReservationCountSql = "SELECT COUNT(*) FROM `reservation`";
        int count = jdbcTemplate.queryForObject(findReservationCountSql, Integer.class);

        Assertions.assertThat(count).isEqualTo(0);
    }

    @Test
    void existsByTimeIdTest() {
        String insertReservationTimeSql = "INSERT INTO `reservation_time` (`id`, `start_at`) VALUES (?, ?)";
        jdbcTemplate.update(insertReservationTimeSql, 100L, "13:00");

        boolean exist = reservationRepository.existsBySlot_Time_Id(100L);
        assertThat(exist).isFalse();

        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 100L, 1L);

        exist = reservationRepository.existsBySlot_Time_Id(100L);
        assertThat(exist).isTrue();
    }

    @Test
    void existsByThemeIdTest() {
        String insertThemeSql = "INSERT INTO `theme` (`id`, `name`, `description`, `thumbnail_url`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(insertThemeSql, 100L, "방탈출1", "방탈출1 설명", "url.jpg");

        boolean exist = reservationRepository.existsBySlot_Theme_Id(100L);
        assertThat(exist).isFalse();

        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 100L);

        exist = reservationRepository.existsBySlot_Theme_Id(100L);
        assertThat(exist).isTrue();
    }

    @Test
    void findByDateAndTimeIdAndThemeIdTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        Optional<Reservation> slot = reservationRepository.findBySlot(
                LocalDate.of(2026, 5, 2), 1L,
                1L);

        assertThat(slot).isNotEmpty();
        assertThat(slot.get().getDate()).isEqualTo(LocalDate.of(2026, 5, 2));
        assertThat(slot.get().getName()).isEqualTo("fizz");
        assertThat(slot.get().getTime().getId()).isEqualTo(1L);
        assertThat(slot.get().getTheme().getId()).isEqualTo(1L);
    }
}
