package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;

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
        return Wait.of(id, wait);
    }

    @Override
    public Optional<Wait> findById(Long id) {
        String sql =
                "SELECT w.id, w.name, w.date, t.id as time_id, t.start_at as time_value, th.id as theme_id, th.name as theme_name, th.description as theme_description, th.thumbnail_url as theme_thumbnail_url "
                        + "FROM `wait` w "
                        + "INNER JOIN `reservation_time` t ON w.time_id = t.id "
                        + "INNER JOIN `theme` th ON w.theme_id = th.id "
                        + "WHERE w.id = (?)";

        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, waitRowMapper(), id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Wait> findBySlot(LocalDate reservationDate, Long timeId, Long themeId) {
        String sql = "SELECT " +
                "w.id, w.created_at, w.name, w.reservation_date AS date, " +
                "rt.id AS time_id, rt.start_at AS time_value, " +
                "t.id AS theme_id, t.name AS theme_name, " +
                "t.description AS theme_description, t.thumbnail_url AS theme_thumbnail_url " +
                "FROM wait w " +
                "JOIN reservation_time rt ON w.time_id = rt.id " +
                "JOIN theme t ON w.theme_id = t.id " +
                "WHERE w.reservation_date = ? AND w.time_id = ? AND w.theme_id = ? " +
                "ORDER BY w.created_at";

        return jdbcTemplate.query(sql, waitRowMapper(),
                reservationDate,
                timeId,
                themeId);
    }

    @Override
    public List<Wait> findByName(String name) {
        String sql = "SELECT " +
                "w.id, w.created_at, w.name, w.reservation_date AS date, " +
                "rt.id AS time_id, rt.start_at AS time_value, " +
                "t.id AS theme_id, t.name AS theme_name, " +
                "t.description AS theme_description, t.thumbnail_url AS theme_thumbnail_url " +
                "FROM wait w " +
                "JOIN reservation_time rt ON w.time_id = rt.id " +
                "JOIN theme t ON w.theme_id = t.id " +
                "WHERE w.name = ? " +
                "ORDER BY w.created_at";

        return jdbcTemplate.query(sql, waitRowMapper(), name);
    }

    @Override
    public List<Wait> findAll() {
        String sql = "SELECT " +
                "w.id, w.created_at, w.name, w.reservation_date AS date, " +
                "rt.id AS time_id, rt.start_at AS time_value, " +
                "t.id AS theme_id, t.name AS theme_name, " +
                "t.description AS theme_description, t.thumbnail_url AS theme_thumbnail_url " +
                "FROM wait w " +
                "JOIN reservation_time rt ON w.time_id = rt.id " +
                "JOIN theme t ON w.theme_id = t.id " +
                "ORDER BY w.created_at";

        return jdbcTemplate.query(sql, waitRowMapper());
    }

    @Override
    public Long findOrderByWait(Wait wait) {
        String sql = "WITH slot_waiting_list AS (" +
                "SELECT `name`, ROW_NUMBER() OVER (ORDER BY created_at ASC) AS `order` " +
                "FROM wait " +
                "WHERE `reservation_date` = ? AND `time_id` = ? AND `theme_id` = ?" +
                ") " +
                "SELECT `order` FROM slot_waiting_list WHERE `name` = ?";

        return jdbcTemplate.queryForObject(sql, Long.class,
                wait.getReservationDate(),
                wait.getTime().getId(),
                wait.getTheme().getId(),
                wait.getName());
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM `wait` WHERE `id` = (?)";

        jdbcTemplate.update(sql, id);
    }

    private static RowMapper<Wait> waitRowMapper() {
        return (resultSet, rowNum) -> {
            Long id = resultSet.getLong("id");
            LocalDateTime createdAt = resultSet.getObject("created_at", LocalDateTime.class);
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
            return new Wait(id, createdAt, name, date, reservationTime, theme);
        };
    }
}
