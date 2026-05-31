package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Waiting waiting) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(waiting);
        insert.execute(params);
    }

    @Override
    public void deleteById(Long id) {
        String sql = """
                DELETE FROM waiting 
                WHERE id = ? 
                """;

        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean isExists(Waiting waiting) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM waiting 
                    WHERE name = ? 
                    AND date = ? 
                    AND time_id = ? 
                    AND theme_id = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, waiting.getName(), waiting.getDate(),
                waiting.getTimeSlotId(),
                waiting.getThemeId());
    }

    @Override
    public List<Waiting> findByName(String name) {
        String sql = """
                SELECT *
                FROM (
                    SELECT id,
                           name,
                           date,
                           time_id,
                           theme_id,
                           ROW_NUMBER() OVER (
                               PARTITION BY date, time_id, theme_id
                               ORDER BY created_at ASC, id ASC
                           ) AS waiting_number
                    FROM waiting
                ) ranked
                WHERE ranked.name = ?
                """;

        return jdbcTemplate.query(sql, rowMapper(), name);
    }

    @Override
    public Optional<Waiting> findById(long waitingId) {
        String sql = """
                SELECT *
                FROM (
                    SELECT id,
                           name,
                           date,
                           time_id,
                           theme_id,
                           ROW_NUMBER() OVER (
                               PARTITION BY date, time_id, theme_id
                               ORDER BY created_at ASC, id ASC
                           ) AS waiting_number
                    FROM waiting
                ) ranked
                WHERE ranked.id = ?
                """;

        List<Waiting> waitings = jdbcTemplate.query(sql, rowMapper(), waitingId);
        return Optional.ofNullable(DataAccessUtils.singleResult(waitings));
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingColumns("name", "date", "time_id", "theme_id");
    }

    private Map<String, Object> createParams(Waiting waiting) {
        return Map.of(
                "name", waiting.getName(),
                "date", waiting.getDate(),
                "time_id", waiting.getTimeSlotId(),
                "theme_id", waiting.getThemeId()
        );
    }

    private RowMapper<Waiting> rowMapper() {
        return (rs, rowNum) -> new Waiting(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getObject("date", LocalDate.class),
                rs.getLong("time_id"),
                rs.getLong("theme_id"),
                rs.getInt("waiting_number")
        );
    }
}
