package roomescape.waiting.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.time.ReservationTime;
import roomescape.waiting.ReservationWaiting;

@Repository
public class ReservationWaitingDao {
    private static final RowMapper<ReservationWaiting> rowMapper = (rs, rowNum) ->
            new ReservationWaiting(
                    rs.getLong("reservation_waiting_id"),
                    rs.getString("name"),
                    rs.getLong("theme_id"),
                    rs.getDate("date").toLocalDate(),
                    new ReservationTime(rs.getLong("time_id"),
                            rs.getTime("start_at").toLocalTime()),
                    rs.getLong("waiting_number")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationWaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_waiting")
                .usingGeneratedKeyColumns("id");
    }

    public Optional<ReservationWaiting> selectById(Long id) {
        String sql = """
                select w.id as reservation_waiting_id, w.name, w.theme_id, w.date, t.id as time_id, t.start_at as start_at, w.waiting_number
                from reservation_waiting w
                join reservation_time t
                on w.time_id = t.id
                where w.id = ?
                """;

        List<ReservationWaiting> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.stream().findFirst();
    }

    public List<ReservationWaiting> selectByName(String name) {
        String sql = """
                select w.id as reservation_waiting_id, w.name, w.theme_id, w.date, t.id as time_id, t.start_at as start_at, w.waiting_number
                from reservation_waiting w
                join reservation_time t
                on w.time_id = t.id
                where w.name = ?
                """;

        return jdbcTemplate.query(sql, rowMapper, name);
    }

    public boolean existsByNameAndDateAndThemeIdAndTimeId(String name, Long themeId, LocalDate date, Long timeId) {
        String sql = """
                SELECT EXISTS (
                                SELECT 1
                                    FROM reservation_waiting
                                    WHERE name = ? AND theme_id = ? AND date = ? AND time_id = ?
                            )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, name, themeId, date, timeId) == Boolean.TRUE;
    }

    public ReservationWaiting insert(ReservationWaiting reservationsWaiting) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", reservationsWaiting.getName())
                .addValue("theme_id", reservationsWaiting.getThemeId())
                .addValue("date", reservationsWaiting.getDate())
                .addValue("time_id", reservationsWaiting.getTime().getId())
                .addValue("waiting_number", reservationsWaiting.getWaitingNumber());

        Long id = (long) simpleJdbcInsert.executeAndReturnKey(parameters);
        return new ReservationWaiting(id, reservationsWaiting.getName(), reservationsWaiting.getThemeId(),
                reservationsWaiting.getDate(),
                reservationsWaiting.getTime(), reservationsWaiting.getWaitingNumber());
    }

    public Long findNextWaitingNumber(Long themeId, LocalDate date, Long timeId) {
        String sql = """
                SELECT COALESCE(MAX(waiting_number), 0) + 1
                FROM reservation_waiting
                WHERE theme_id = ? AND date = ? AND time_id = ?
                """;

        return jdbcTemplate.queryForObject(sql, Long.class, themeId, date, timeId);
    }

    public void deleteById(Long id) {
        String sql = "delete from reservation_waiting where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
