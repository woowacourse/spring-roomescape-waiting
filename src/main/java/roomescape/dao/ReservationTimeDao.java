package roomescape.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.dto.ReservationTimeAvailability;
import roomescape.domain.ReservationTime;

@Repository
public class ReservationTimeDao {

    private static final RowMapper<ReservationTime> ROW_MAPPER = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    private static final RowMapper<ReservationTimeAvailability> TIME_AVAILABILITY_ROW_MAPPER = (resultSet, rowNum) ->
            new ReservationTimeAvailability(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime(),
                    resultSet.getBoolean("reserved")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime save(ReservationTime reservationTime) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start_at", reservationTime.getStartAt());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return reservationTime.createWithId(generatedId.longValue());
    }

    public List<ReservationTime> findAll() {
        String sql = """
                SELECT id, 
                       start_at
                FROM reservation_time
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public Optional<ReservationTime> findById(Long reservationTimeId) {
        String sql = """
                SELECT id, 
                       start_at
                FROM reservation_time
                WHERE id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, reservationTimeId));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    public List<ReservationTimeAvailability> findAvailabilitiesByThemeIdAndDate(long themeId, LocalDate date) {
        String sql = """
                SELECT rt.id,
                       rt.start_at,
                       CASE
                           WHEN r.id IS NULL THEN false
                           ELSE true
                       END AS reserved
                FROM reservation_time AS rt
                LEFT JOIN slot AS s
                    ON s.time_id = rt.id
                   AND s.theme_id = ?
                   AND s.date = ?
                LEFT JOIN reservation AS r
                    ON r.slot_id = s.id
                ORDER BY rt.start_at
                """;
        return jdbcTemplate.query(sql, TIME_AVAILABILITY_ROW_MAPPER, themeId, date);
    }


    public boolean existsByStartAt(LocalTime startAt) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation_time
                    WHERE start_at = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, startAt));
    }

    public int delete(long id) {
        String sql = """
                DELETE FROM reservation_time
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql, id);
    }
}
