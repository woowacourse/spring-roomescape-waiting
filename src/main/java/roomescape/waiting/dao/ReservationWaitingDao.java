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
                    rs.getLong("id"),
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
                select id, name, theme_id, date, time_id, start_at, waiting_number
                from (
                    select w.id, w.name, w.theme_id, w.date, t.id as time_id, t.start_at as start_at,
                        ROW_NUMBER() OVER (PARTITION BY w.theme_id, w.date, w.time_id ORDER BY w.id) AS waiting_number
                    from reservation_waiting w
                    join reservation_time t
                    on w.time_id = t.id
                ) ranked
                where id = ?
                """;

        List<ReservationWaiting> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.stream().findFirst();
    }

    public List<ReservationWaiting> selectByName(String name) {
        String sql = """
                select id, name, theme_id, date, time_id, start_at, waiting_number
                from (
                    select w.id, w.name, w.theme_id, w.date, t.id as time_id, t.start_at as start_at,
                        ROW_NUMBER() OVER (PARTITION BY w.theme_id, w.date, w.time_id ORDER BY w.id) AS waiting_number
                    from reservation_waiting w
                    join reservation_time t
                    on w.time_id = t.id
                ) ranked
                where name = ?
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
                .addValue("time_id", reservationsWaiting.getTime().getId());

        Long id = (long) simpleJdbcInsert.executeAndReturnKey(parameters);
        return selectById(id)
                .orElseThrow(() -> new IllegalStateException("삽입된 예약 대기 데이터를 찾을 수 없습니다. id : " + id));
    }

    public void deleteById(Long id) {
        String sql = "delete from reservation_waiting where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
