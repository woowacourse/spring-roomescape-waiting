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
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.repository.dto.WaitDetailDto;

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
                LocalDate.of(2026, 5, 27), reservationTime, theme);

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

        Optional<WaitDetailDto> waitLuke = waitRepository.findById(1L);
        Optional<WaitDetailDto> waitFizz = waitRepository.findById(2L);
        Optional<WaitDetailDto> waitNeo = waitRepository.findById(3L);

        assertThat(waitLuke).isNotEmpty();
        assertThat(waitLuke.get().order()).isEqualTo(1L);

        assertThat(waitFizz).isNotEmpty();
        assertThat(waitFizz.get().order()).isEqualTo(2L);

        assertThat(waitNeo).isNotEmpty();
        assertThat(waitNeo.get().order()).isEqualTo(3L);
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

        List<WaitDetailDto> slots = waitRepository.findBySlot(LocalDate.of(2026, 5, 27), reservationTime.getId(),
                theme.getId());

        assertThat(slots.size()).isEqualTo(3);
        assertThat(slots.get(0).order()).isEqualTo(1L);
        assertThat(slots.get(1).order()).isEqualTo(2L);
        assertThat(slots.get(2).order()).isEqualTo(3L);
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

        List<WaitDetailDto> waits = waitRepository.findByName("luke");

        assertThat(waits.size()).isEqualTo(3);

        assertThat(waits.get(0).order()).isEqualTo(1L);
        assertThat(waits.get(0).name()).isEqualTo("luke");

        assertThat(waits.get(1).order()).isEqualTo(2L);
        assertThat(waits.get(1).name()).isEqualTo("luke");

        assertThat(waits.get(2).order()).isEqualTo(3L);
        assertThat(waits.get(2).name()).isEqualTo("luke");
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

        List<WaitDetailDto> waits = waitRepository.findAll();

        assertThat(waits.size()).isEqualTo(3);

        assertThat(waits.get(0).order()).isEqualTo(1L);
        assertThat(waits.get(1).order()).isEqualTo(2L);
        assertThat(waits.get(2).order()).isEqualTo(3L);
    }

    @Test
    void findOrderByWaitTest() {
        String createWait = "INSERT INTO `wait`(`created_at`, `name`, `reservation_date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 0),
                "luke", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 1),
                "fizz", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());
        jdbcTemplate.update(createWait, LocalDateTime.of(2026, 5, 21, 10, 2),
                "neo", LocalDate.of(2026, 5, 27), reservationTime.getId(), theme.getId());

        Wait waitLuke = new Wait(1L, LocalDateTime.of(2026, 5, 21, 10, 0), "luke",
                LocalDate.of(2026, 5, 27), reservationTime, theme);
        Wait waitFizz = new Wait(2L, LocalDateTime.of(2026, 5, 21, 10, 1), "fizz",
                LocalDate.of(2026, 5, 27), reservationTime, theme);
        Wait waitNeo = new Wait(2L, LocalDateTime.of(2026, 5, 21, 10, 2), "neo",
                LocalDate.of(2026, 5, 27), reservationTime, theme);

        assertThat(waitRepository.findOrderByWait(waitLuke)).isEqualTo(1L);
        assertThat(waitRepository.findOrderByWait(waitFizz)).isEqualTo(2L);
        assertThat(waitRepository.findOrderByWait(waitNeo)).isEqualTo(3L);
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
