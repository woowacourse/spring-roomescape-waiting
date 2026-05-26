package roomescape.repository;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.service.dto.WaitingCommand;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int calculateWaitingNumber(WaitingCommand waiting) {
        String sql = """
                SELECT COUNT(*)
                FROM waiting w
                WHERE w.date = ?
                  AND w.time_id = ?
                  AND w.theme_id = ?
                  AND w.created_at <= (
                      SELECT target.created_at
                      FROM waiting target
                      WHERE target.name = ?
                        AND target.date = ?
                        AND target.time_id = ?
                        AND target.theme_id = ?
                  )
                """;

        return jdbcTemplate.queryForObject(sql, Integer.class, waiting.date(), waiting.timeId(), waiting.themeId(),
                waiting.name(), waiting.date(), waiting.timeId(), waiting.themeId());
    }

    @Override
    public void save(WaitingCommand waiting) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(waiting);
        insert.execute(params);
    }

    @Override
    public void delete(WaitingCommand waiting) {
        String sql = """
                DELETE FROM waiting 
                WHERE name = ? 
                AND date = ? 
                AND time_id = ? 
                AND theme_id = ?
                """;

        jdbcTemplate.update(sql, waiting.name(), waiting.date(), waiting.timeId(), waiting.themeId());
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingColumns("name", "date", "time_id", "theme_id");
    }

    private Map<String, Object> createParams(WaitingCommand waiting) {
        return Map.of(
                "name", waiting.name(),
                "date", waiting.date(),
                "time_id", waiting.timeId(),
                "theme_id", waiting.themeId()
        );
    }
}
