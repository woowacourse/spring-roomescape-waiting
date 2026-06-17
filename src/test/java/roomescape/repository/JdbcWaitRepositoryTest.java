package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.domain.Waits;

@JdbcTest
class JdbcWaitRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private WaitRepository waitRepository;
    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void beforeEach() {
        waitRepository = new JdbcWaitRepository(jdbcTemplate);

        String insertReservationTimeSql = "INSERT INTO `reservation_time` (`start_at`) VALUES (?)";
        jdbcTemplate.update(insertReservationTimeSql, "10:00");

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
        Wait waitWithoutId = new Wait(LocalDateTime.of(2026, 5, 21, 10, 0), "luke",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme));

        Wait wait = waitRepository.save(waitWithoutId);

        assertThat(wait.getId()).isEqualTo(1L);
        assertThat(wait.getName()).isEqualTo("luke");
    }

    @Test
    void findByIdTest() {
        String createWait = "INSERT INTO `wait`(`created_at`, `name`, `reservation_date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 0),
                "luke", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 1),
                "fizz", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 2),
                "neo", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());

        Optional<Wait> waitLuke = waitRepository.findById(1L);
        Optional<Wait> waitFizz = waitRepository.findById(2L);
        Optional<Wait> waitNeo = waitRepository.findById(3L);

        assertThat(waitLuke).isNotEmpty();

        assertThat(waitFizz).isNotEmpty();

        assertThat(waitNeo).isNotEmpty();
    }

    @Test
    void findBySlotTest() {
        String createWait = "INSERT INTO `wait`(`created_at`, `name`, `reservation_date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 0),
                "luke", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 1),
                "fizz", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 2),
                "neo", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());

        Waits slots = waitRepository.findBySlot(LocalDate.of(2026, 5, 27), reservationTime.getId(),
                theme.getId());

        assertThat(slots.size()).isEqualTo(3);
    }

    @Test
    void findByNameTest() {
        String createWait = "INSERT INTO `wait`(`created_at`, `name`, `reservation_date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 0),
                "luke", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());

        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 0),
                "fizz", LocalDate.of(2026, 5, 28), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 1),
                "luke", LocalDate.of(2026, 5, 28), reservationTime.getId(), theme.getId());

        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 0),
                "fizz", LocalDate.of(2026, 5, 29), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 1),
                "neo", LocalDate.of(2026, 5, 29), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 2),
                "luke", LocalDate.of(2026, 5, 29), reservationTime.getId(), theme.getId());

        Waits waits = waitRepository.findByName("luke");

        assertThat(waits.size()).isEqualTo(3);

        assertThat(waits.getWaits().get(0).getName()).isEqualTo("luke");

        assertThat(waits.getWaits().get(1).getName()).isEqualTo("luke");

        assertThat(waits.getWaits().get(2).getName()).isEqualTo("luke");
    }

    @Test
    void findAllTest() {
        String createWait = "INSERT INTO `wait`(`created_at`, `name`, `reservation_date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 0),
                "luke", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 1),
                "fizz", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 2),
                "neo", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());

        Waits waits = waitRepository.findAll();

        assertThat(waits.size()).isEqualTo(3);
    }

    @Test
    void calculateWaitingOrderTest() {
        String createWait = "INSERT INTO `wait`(`created_at`, `name`, `reservation_date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 0),
                "luke", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 1),
                "fizz", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 2),
                "neo", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());

        Slot slot = new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme);
        Wait waitLuke = new Wait(1L, LocalDateTime.of(2026, 5, 21, 10, 0), "luke", slot);
        Wait waitFizz = new Wait(2L, LocalDateTime.of(2026, 5, 21, 10, 1), "fizz", slot);
        Wait waitNeo = new Wait(3L, LocalDateTime.of(2026, 5, 21, 10, 2), "neo", slot);

        assertThat(waitRepository.calculateWaitingOrder(waitLuke)).isEqualTo(1L);
        assertThat(waitRepository.calculateWaitingOrder(waitFizz)).isEqualTo(2L);
        assertThat(waitRepository.calculateWaitingOrder(waitNeo)).isEqualTo(3L);
    }

    @Test
    void deleteByIdTest() {
        String createWait = "INSERT INTO `wait`(`created_at`, `name`, `reservation_date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 0),
                "luke", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());

        waitRepository.deleteById(1L);

        String findWaitCountSql = "SELECT COUNT(*) FROM `wait`";
        int count = jdbcTemplate.queryForObject(findWaitCountSql, Integer.class);

        Assertions.assertThat(count).isEqualTo(0);
    }
}
