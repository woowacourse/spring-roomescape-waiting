package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationWaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReservationWaiting> findByName(String name) {
        String sql = """
                SELECT
                    r.id as reservation_waiting_id,
                    r.name as username,
                    r.date,
                    rt.id as time_id,
                    rt.start_at as time_value,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail
                FROM reservation_waiting as r
                INNER JOIN reservation_time as rt
                  ON r.time_id = rt.id
                INNER JOIN theme as t
                  ON r.theme_id = t.id
                WHERE r.name = ?
                ORDER BY r.id;
                """;
        return jdbcTemplate.query(sql, waitingRowMapper, name);
    }

    public Long countEarlierWaitings(Long id) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation_waiting AS earlier
                INNER JOIN reservation_waiting AS target
                    ON target.id = ?
                WHERE earlier.date = target.date
                    AND earlier.time_id = target.time_id
                    AND earlier.theme_id = target.theme_id
                    AND (
                        earlier.created_at < target.created_at
                        OR (
                            earlier.created_at = target.created_at
                            AND earlier.id < target.id
                        )
                    );
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, id);

        if (count == null) {
            return 0L;
        }
        return count;
    }

    public Long insert(ReservationWaiting waiting) {
        String sql = "INSERT INTO reservation_waiting(name, date, time_id, theme_id) VALUES (?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(
                    sql,
                    new String[]{"id"});
            pstmt.setString(1, waiting.getName());
            pstmt.setObject(2, waiting.getDate());
            pstmt.setLong(3, waiting.getTime().getId());
            pstmt.setLong(4, waiting.getTheme().getId());
            return pstmt;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public boolean existsByNameWith(String name, LocalDate date, Long timeId, Long themeId) {
        String sql = "SELECT count(*) FROM reservation_waiting WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, date, timeId, themeId);
        return count != null && count > 0;
    }

    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT count(*) FROM reservation_waiting WHERE time_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return count != null && count > 0;
    }

    public Optional<ReservationWaiting> findById(Long id) {
        String sql = """
                SELECT
                    r.id as reservation_waiting_id,
                    r.name as username,
                    r.date,
                    rt.id as time_id,
                    rt.start_at as time_value,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail
                FROM reservation_waiting as r
                INNER JOIN reservation_time as rt
                  ON r.time_id = rt.id
                INNER JOIN theme as t
                  ON r.theme_id = t.id
                WHERE r.id = ?;
                """;
        List<ReservationWaiting> result = jdbcTemplate.query(sql, waitingRowMapper, id);
        return result.stream().findAny();
    }

    public void delete(Long id) {
        String sql = "DELETE FROM reservation_waiting WHERE id = ?;";
        jdbcTemplate.update(sql, id);
    }

    private final RowMapper<ReservationWaiting> waitingRowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("time_value", LocalTime.class));

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail"));

        ReservationWaiting waiting = new ReservationWaiting(
                resultSet.getLong("reservation_waiting_id"),
                resultSet.getString("username"),
                resultSet.getObject("date", LocalDate.class),
                time,
                theme);
        return waiting;
    };
}
