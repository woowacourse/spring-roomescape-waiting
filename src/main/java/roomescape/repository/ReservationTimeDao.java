package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Repository
public class ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;
    private final RowMapper<ReservationTime> rowMapper = (rs, rowNum) ->
            ReservationTime.create(
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

        return ReservationTime.create(
                newId.longValue(),
                startAt
        );
    }

    public void deleteByTimeId(long timeId) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        int affected = jdbcTemplate.update(sql, timeId);

        if(affected == 0) {
            throw new NoSuchElementException("[ERROR] 삭제할 id에 해당하는 시간이 존재하지 않습니다.");
        }
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
