package roomescape.infrastructure;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Time;
import roomescape.domain.repository.TimeRepository;

@Repository
public class JdbcTimeRepository implements TimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Time> timeRowMapper = (rs, rowNum) -> new Time(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime()
    );

    public JdbcTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public List<Time> findAll() {
        return jdbcTemplate.query("SELECT id, start_at FROM reservation_time", timeRowMapper);
    }

    public Long save(LocalTime startAt) {
        return jdbcInsert.executeAndReturnKey(Map.of("start_at", startAt)).longValue();
    }

    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
    }
}
