package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Wait;
import roomescape.repository.dto.ReservationTimeDto;
import roomescape.repository.dto.ThemeDto;
import roomescape.repository.dto.WaitDetailDto;

@Repository
public class JdbcWaitRepository implements WaitRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Wait save(Wait wait) {
        String sql = "INSERT INTO `wait`(`created_at`, `name`, `reservation_date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setObject(1, wait.getCreatedAt());
            preparedStatement.setString(2, wait.getName());
            preparedStatement.setDate(3, Date.valueOf(wait.getReservationDate()));
            preparedStatement.setLong(4, wait.getTime().getId());
            preparedStatement.setLong(5, wait.getTheme().getId());

            return preparedStatement;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return Wait.withId(id, wait);
    }

    @Override
    public Optional<WaitDetailDto> findById(Long id) {
        String sql = """
                SELECT w.id, w.created_at, w.name, w.reservation_date,
                t.id AS time_id, t.start_at AS time_value,
                th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail_url,
                (
                    SELECT COUNT(*) + 1
                    FROM wait w2
                    WHERE w2.reservation_date = w.reservation_date
                      AND w2.time_id = w.time_id
                      AND w2.theme_id = w.theme_id
                      AND (w2.created_at < w.created_at OR (w2.created_at = w.created_at AND w2.id < w.id))
                ) AS wait_order
                FROM wait w
                INNER JOIN reservation_time t ON w.time_id = t.id
                INNER JOIN theme th ON w.theme_id = th.id
                WHERE w.id = (?)
                """;

        return jdbcTemplate.query(sql, waitDetailDtoRowMapper(), id)
                .stream()
                .findFirst();
    }

    @Override
    public List<WaitDetailDto> findBySlot(LocalDate reservationDate, Long timeId, Long themeId) {
        String sql = """
                SELECT w.id, w.created_at, w.name, w.reservation_date,
                rt.id AS time_id, rt.start_at AS time_value,
                t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.thumbnail_url AS theme_thumbnail_url,
                ROW_NUMBER() OVER (
                    PARTITION BY w.reservation_date, w.time_id, w.theme_id
                    ORDER BY w.created_at, w.id
                ) AS wait_order
                FROM wait w
                JOIN reservation_time rt ON w.time_id = rt.id
                JOIN theme t ON w.theme_id = t.id
                WHERE w.reservation_date = ? AND w.time_id = ? AND w.theme_id = ?
                ORDER BY w.created_at, w.id
                """;

        return jdbcTemplate.query(sql, waitDetailDtoRowMapper(),
                reservationDate,
                timeId,
                themeId);
    }

    @Override
    public List<WaitDetailDto> findByName(String name) {
        String sql = """
                SELECT w.id, w.created_at, w.name, w.reservation_date,
                rt.id AS time_id, rt.start_at AS time_value,
                t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.thumbnail_url AS theme_thumbnail_url,
                (
                    SELECT COUNT(*) + 1
                    FROM wait w2
                    WHERE w2.reservation_date = w.reservation_date
                      AND w2.time_id = w.time_id
                      AND w2.theme_id = w.theme_id
                      AND (w2.created_at < w.created_at OR (w2.created_at = w.created_at AND w2.id < w.id))
                ) AS wait_order
                FROM wait w
                JOIN reservation_time rt ON w.time_id = rt.id
                JOIN theme t ON w.theme_id = t.id
                WHERE w.name = ?
                ORDER BY w.created_at, w.id
                """;

        return jdbcTemplate.query(sql, waitDetailDtoRowMapper(), name);
    }

    @Override
    public List<WaitDetailDto> findAll() {
        String sql = """
                SELECT w.id, w.created_at, w.name, w.reservation_date,
                rt.id AS time_id, rt.start_at AS time_value,
                t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.thumbnail_url AS theme_thumbnail_url,
                ROW_NUMBER() OVER (
                    PARTITION BY w.reservation_date, w.time_id, w.theme_id
                    ORDER BY w.created_at, w.id
                ) AS wait_order
                FROM wait w
                JOIN reservation_time rt ON w.time_id = rt.id
                JOIN theme t ON w.theme_id = t.id
                ORDER BY w.created_at, w.id
                """;

        return jdbcTemplate.query(sql, waitDetailDtoRowMapper());
    }

    @Override
    public Long findOrderByWait(Wait wait) {
        String sql = """
                WITH slot_waiting_list AS (
                    SELECT `name`, ROW_NUMBER() OVER (ORDER BY created_at, id) AS `order`
                    FROM wait
                    WHERE `reservation_date` = ? AND `time_id` = ? AND `theme_id` = ?
                )
                SELECT `order` FROM slot_waiting_list WHERE `name` = ?
                """;

        return jdbcTemplate.queryForObject(sql, Long.class,
                wait.getReservationDate(),
                wait.getTime().getId(),
                wait.getTheme().getId(),
                wait.getName());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM `wait` WHERE `id` = (?)";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM `wait` WHERE `time_id` = (?)) AS exist";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, timeId));
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM `wait` WHERE `theme_id` = (?)) AS exist";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeId));
    }

    private static RowMapper<WaitDetailDto> waitDetailDtoRowMapper() {
        return (resultSet, rowNum) -> {
            Long id = resultSet.getLong("id");
            LocalDateTime createdAt = resultSet.getObject("created_at", LocalDateTime.class);
            String name = resultSet.getString("name");
            LocalDate date = resultSet.getDate("reservation_date").toLocalDate();
            Long timeId = resultSet.getLong("time_id");
            LocalTime timeValue = resultSet.getTime("time_value").toLocalTime();
            Long themeId = resultSet.getLong("theme_id");
            String themeName = resultSet.getString("theme_name");
            String themeDescription = resultSet.getString("theme_description");
            String themeThumbnailUrl = resultSet.getString("theme_thumbnail_url");
            Long order = resultSet.getLong("wait_order");

            ReservationTimeDto reservationTimeDto = new ReservationTimeDto(timeId, timeValue);
            ThemeDto themeDto = new ThemeDto(themeId, themeName, themeDescription, themeThumbnailUrl);
            return new WaitDetailDto(id, createdAt, name, date, reservationTimeDto, themeDto, order);
        };
    }
}
