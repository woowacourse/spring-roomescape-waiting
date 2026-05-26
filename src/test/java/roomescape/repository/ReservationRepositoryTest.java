package roomescape.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public class ReservationRepositoryTest extends RepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    private ReservationTime reservationTime;

    private Theme theme;

    @BeforeEach
    void beforeEach() {
        String insertReservationTimeSql = "INSERT INTO `reservation_time` (`start_at`) VALUES (?)";
        jdbcTemplate.update(insertReservationTimeSql, "10:00");
        jdbcTemplate.update(insertReservationTimeSql, "11:00");
        jdbcTemplate.update(insertReservationTimeSql, "12:00");

        String insertThemeSql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertThemeSql, "방탈출1", "방탈출1 설명", "url.jpg");

        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "방탈출1", "방탈출1 설명", "url.jpg");
    }

    @Test
    void createTest() {
        Reservation reservationWithoutId = new Reservation("fizz", LocalDate.of(2026, 5, 2), reservationTime, theme);

        Reservation reservation = reservationRepository.create(reservationWithoutId);

        assertThat(reservation.getId()).isEqualTo(1L);
    }

    @Test
    void readByIdTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        Reservation reservation = reservationRepository.readById(1L).get();

        assertThat(reservation.getName()).isEqualTo("fizz");
        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2026, 5, 2));
        assertThat(reservation.getTime().getId()).isEqualTo(1L);
        assertThat(reservation.getTheme().getId()).isEqualTo(1L);
    }

    @Test
    void readByNameTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);
        jdbcTemplate.update(sql, "tree", "2026-05-02", 2L, 1L);
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 3L, 1L);

        List<Reservation> reservations = reservationRepository.readByName("fizz");

        assertThat(reservations.size()).isEqualTo(2);
        assertThat(reservations.get(0).getName()).isEqualTo("fizz");
        assertThat(reservations.get(1).getName()).isEqualTo("fizz");

        assertThat(reservationRepository.readByName("user").size()).isEqualTo(0);
    }

    @Test
    void readAllTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 2L, 1L);

        List<Reservation> reservations = reservationRepository.readAll();

        assertThat(reservations.size()).isEqualTo(2);
    }

    @Test
    void updateTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        LocalDate now = LocalDate.now();

        Long id = 1L;
        LocalDate newDate = now.plusDays(1);
        Long newTimeId = 2L;

        reservationRepository.update(id, newDate, 2L);

        String selectSql =
                "SELECT r.id, r.name, r.date, t.id as time_id, t.start_at as time_value, th.id as theme_id, th.name as theme_name, th.description as theme_description, th.thumbnail_url as theme_thumbnail_url "
                        + "FROM `reservation` r "
                        + "INNER JOIN `reservation_time` t ON r.time_id = t.id "
                        + "INNER JOIN `theme` th ON r.theme_id = th.id "
                        + "WHERE r.id = (?)";

        Reservation reservation = jdbcTemplate.queryForObject(selectSql, reservationRowMapper(), id);

        org.junit.jupiter.api.Assertions.assertNotNull(reservation);
        assertThat(reservation.getName()).isEqualTo("fizz");
        assertThat(reservation.getDate()).isEqualTo(newDate);
        assertThat(reservation.getTime().getId()).isEqualTo(newTimeId);
        assertThat(reservation.getTheme().getId()).isEqualTo(1L);
    }

    private static RowMapper<Reservation> reservationRowMapper() {
        return (resultSet, rowNum) -> {
            Long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            LocalDate date = resultSet.getDate("date").toLocalDate();
            Long timeId = resultSet.getLong("time_id");
            LocalTime timeValue = resultSet.getTime("time_value").toLocalTime();
            Long themeId = resultSet.getLong("theme_id");
            String themeName = resultSet.getString("theme_name");
            String themeDescription = resultSet.getString("theme_description");
            String themeThumbnailUrl = resultSet.getString("theme_thumbnail_url");

            ReservationTime reservationTime = new ReservationTime(timeId, timeValue);
            Theme theme = new Theme(themeId, themeName, themeDescription, themeThumbnailUrl);
            return new Reservation(id, name, date, reservationTime, theme);
        };
    }

    @Test
    void deleteTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        reservationRepository.delete(1L);

        String readReservationCountSql = "SELECT COUNT(*) FROM `reservation`";
        int count = jdbcTemplate.queryForObject(readReservationCountSql, Integer.class);

        Assertions.assertThat(count).isEqualTo(0);
    }

    @Test
    void existByDateAndTimeIdAndThemeIdTest() {
        boolean exist = reservationRepository.existByDateAndTimeIdAndThemeId(LocalDate.of(2026, 5, 2), 1L, 1L);
        assertThat(exist).isFalse();

        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        exist = reservationRepository.existByDateAndTimeIdAndThemeId(LocalDate.of(2026, 5, 2), 1L, 1L);
        assertThat(exist).isTrue();
    }

    @Test
    void existByTimeIdTest() {
        boolean exist = reservationRepository.existByTimeId(1L);
        assertThat(exist).isFalse();

        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        exist = reservationRepository.existByTimeId(1L);
        assertThat(exist).isTrue();
    }

    @Test
    void existByThemeIdTest() {
        boolean exist = reservationRepository.existByThemeId(1L);
        assertThat(exist).isFalse();

        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        exist = reservationRepository.existByThemeId(1L);
        assertThat(exist).isTrue();
    }
}
