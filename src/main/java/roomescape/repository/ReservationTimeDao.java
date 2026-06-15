package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReservationTimeDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;
    private final RowMapper<ReservationTime> rowMapper = (rs, rowNum) ->
            ReservationTime.create(
                    rs.getLong("id"),
                    rs.getObject("start_at", LocalTime.class)
            );

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime save(ReservationTime time) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", time.startAt());

        long id = insertExecutor.executeAndReturnKey(params).longValue();

        return ReservationTime.create(id, time.startAt());
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM reservation_time WHERE id = :id";
        int affected = jdbcTemplate.update(sql, Map.of("id", id));

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 시간을 찾을 수 없습니다.");
        }
    }

    public Optional<ReservationTime> findById(long id) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = :id";
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper)
                .stream()
                .findFirst();
    }

    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(sql, Map.of(), rowMapper);
    }

    public List<ReservationTime> findAvailable(LocalDate date, long themeId) {
        String sql = """
                SELECT rt.id, rt.start_at
                FROM reservation_time rt
                LEFT JOIN reservation r
                    ON rt.id = r.time_id
                    AND r.date = :date
                    AND r.theme_id = :themeId
                WHERE r.id IS NULL
                ORDER BY rt.start_at ASC, rt.id ASC
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId);
        return jdbcTemplate.query(sql, params, rowMapper);
    }
}
