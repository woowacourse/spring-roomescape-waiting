package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
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
    public int calculateWaitingNumber(Waiting waiting) {
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

        return jdbcTemplate.queryForObject(sql, Integer.class,
                waiting.getDate(), waiting.getTimeSlotId(), waiting.getThemeId(),
                waiting.getName(), waiting.getDate(), waiting.getTimeSlotId(), waiting.getThemeId());
    }

    @Override
    public void save(Waiting waiting) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(waiting);
        insert.execute(params);
    }

    @Override
    public void delete(Waiting waiting) {
        String sql = """
                DELETE FROM waiting 
                WHERE name = ? 
                AND date = ? 
                AND time_id = ? 
                AND theme_id = ?
                """;

        jdbcTemplate.update(sql, waiting.getName(), waiting.getDate(), waiting.getTimeSlotId(), waiting.getThemeId());
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
    public int countAllBy(LocalDate date, Long timeSlotId, Long themeId) {
        String sql = """
                SELECT COUNT(*)
                FROM waiting
                WHERE date = ?
                  AND time_id = ?
                  AND theme_id = ?
                """;

        return jdbcTemplate.queryForObject(sql, Integer.class, date, timeSlotId, themeId);
    }

    @Override
    public List<Waiting> findByName(String name) {
        String sql = """
                SELECT *
                FROM waiting
                WHERE name = ?
                """;

        return jdbcTemplate.queryForList(sql, Waiting.class, name);
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
}
