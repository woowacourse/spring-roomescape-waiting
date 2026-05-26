package roomescape.waiting.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.waiting.ReservationWaiting;

import java.time.LocalDate;

@Repository
public class ReservationWaitingDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationWaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_waiting")
                .usingGeneratedKeyColumns("id");
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
                .addValue("time_id", reservationsWaiting.getTimeId())
                .addValue("waiting_number", reservationsWaiting.getWaitingNumber());

        Long id = (long) simpleJdbcInsert.executeAndReturnKey(parameters);
        return new ReservationWaiting(id, reservationsWaiting.getName(), reservationsWaiting.getThemeId(), reservationsWaiting.getDate(),
                reservationsWaiting.getTimeId(), reservationsWaiting.getWaitingNumber());
    }

    public Long findNextWaitingNumber(Long themeId, LocalDate date, Long timeId) {
        String sql = """
                SELECT COALESCE(MAX(waiting_number), 0) + 1
                FROM reservation_waiting
                WHERE theme_id = ? AND date = ? AND time_id = ?
                """;

        return jdbcTemplate.queryForObject(sql, Long.class, themeId, date, timeId);
    }
}
