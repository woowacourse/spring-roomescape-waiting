package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public class ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;
    private final RowMapper<ReservationTime> rowMapper = (rs, rowNum) ->
            ReservationTime.from(
                    rs.getLong("id"),
                    rs.getObject("start_at", LocalTime.class)
            );

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }


    public ReservationTime save(LocalTime startAt) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", startAt);

        Number newId = insertExecutor.executeAndReturnKey(params);

        return ReservationTime.from(
                newId.longValue(),
                startAt
        );
    }

    public void deleteByTimeId(long timeId) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        int affected = jdbcTemplate.update(sql, timeId);

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 시간을 찾을 수 없습니다.");
        }
    }

    public ReservationTime findById(long timeId) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, timeId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("요청한 시간을 찾을 수 없습니다."));
    }

    public List<ReservationTime> findAllReservationTimes() {
        String sql = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<ReservationTime> findAvailableReservationTimes(LocalDate date, long themeId) {
        String sql = """
                SELECT rt.id, rt.start_at
                FROM reservation_time rt
                LEFT JOIN reservation r
                    ON rt.id = r.time_id
                    AND r.date = ?
                    AND r.theme_id = ?
                WHERE r.id IS NULL
                """;
        return jdbcTemplate.query(sql, rowMapper, date, themeId);
    }
}
