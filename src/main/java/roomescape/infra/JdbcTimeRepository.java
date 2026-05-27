package roomescape.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Time;
import roomescape.repository.TimeRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcTimeRepository implements TimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Time> findAll() {
        String sql = "SELECT id, start_at FROM time";
        return jdbcTemplate.query(sql, rowMapper());
    }

    @Override
    public Optional<Time> findById(long timeId) {
        String sql = "SELECT id, start_at FROM time where id = ?";
        return jdbcTemplate.query(sql, rowMapper(), timeId).stream().findFirst();
    }

    @Override
    public Time save(Time time) {
        Map<String, Object> params = createParams(time);
        long reservationId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Time(reservationId, time.getStartAt());
    }

    @Override
    public void deleteById(long timeId) {
        String sql = "DELETE FROM time where id = ?";
        jdbcTemplate.update(sql, timeId);
    }

    private RowMapper<Time> rowMapper() {
        return (rs, rowNum) -> new Time(
                rs.getLong("id"),
                rs.getObject("start_at", LocalTime.class)
        );
    }

    private Map<String, Object> createParams(Time time) {
        return Map.of("start_at", time.getStartAt());
    }
}
