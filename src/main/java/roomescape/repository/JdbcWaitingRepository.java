package roomescape.repository;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private static final String CALCULATE_WAITING_NUMBER_SQL = """
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

    private static final String EXISTS_SQL = """
            SELECT EXISTS (
                SELECT 1
                FROM waiting 
                WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?
            )
            """;

    private static final String DELETE_SQL = """
            DELETE FROM waiting 
            WHERE id = ? 
            """;

    private static final String FIND_BY_NAME_SQL = """
            SELECT 
                w.id AS waiting_id,
                w.name,
                w.date, 
                w.waiting_number,
                t.id AS t_id,
                t.start_at,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail_url
            FROM (
                SELECT id, name, date, time_id, theme_id,
                       ROW_NUMBER() OVER (
                           PARTITION BY date, time_id, theme_id
                           ORDER BY created_at ASC, id ASC
                       ) AS waiting_number
                FROM waiting
            ) w
            INNER JOIN time_slot t ON w.time_id = t.id
            INNER JOIN theme th ON w.theme_id = th.id
            WHERE w.name = ?
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT 
                w.id AS waiting_id,
                w.name,
                w.date, 
                w.waiting_number,
                t.id AS t_id,
                t.start_at,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail_url
            FROM (
                SELECT id, name, date, time_id, theme_id,
                       ROW_NUMBER() OVER (
                           PARTITION BY date, time_id, theme_id
                           ORDER BY created_at ASC, id ASC
                       ) AS waiting_number
                FROM waiting
            ) w
            INNER JOIN time_slot t ON w.time_id = t.id
            INNER JOIN theme th ON w.theme_id = th.id
            WHERE w.id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int calculateWaitingNumber(Waiting waiting) {
        return jdbcTemplate.queryForObject(CALCULATE_WAITING_NUMBER_SQL, Integer.class,
                waiting.getDate(), waiting.getTimeSlot().getId(), waiting.getTheme().getId(),
                waiting.getName(), waiting.getDate(), waiting.getTimeSlot().getId(), waiting.getTheme().getId());
    }

    @Override
    public Waiting save(Waiting waiting) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(waiting);
        long waitingId = insert.executeAndReturnKey(params).longValue();
        return new Waiting(
                waitingId,
                waiting.getName(),
                waiting.getDate(),
                waiting.getTimeSlot(),
                waiting.getTheme(),
                waiting.getWaitingNumber()
        );
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    @Override
    public boolean isExists(Waiting waiting) {
        return jdbcTemplate.queryForObject(EXISTS_SQL, Boolean.class,
                waiting.getName(), waiting.getDate(), waiting.getTimeSlot().getId(), waiting.getTheme().getId());
    }

    @Override
    public List<Waiting> findByName(String name) {
        return jdbcTemplate.query(FIND_BY_NAME_SQL, rowMapper(), name);
    }

    @Override
    public Optional<Waiting> findById(long waitingId) {
        List<Waiting> waitings = jdbcTemplate.query(FIND_BY_ID_SQL, rowMapper(), waitingId);
        return Optional.ofNullable(DataAccessUtils.singleResult(waitings));
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "date", "time_id", "theme_id");
    }

    private Map<String, Object> createParams(Waiting waiting) {
        return Map.of(
                "name", waiting.getName(),
                "date", waiting.getDate(),
                "time_id", waiting.getTimeSlot().getId(),
                "theme_id", waiting.getTheme().getId()
        );
    }

    private RowMapper<Waiting> rowMapper() {
        return (rs, rowNum) -> new Waiting(
                rs.getLong("waiting_id"),
                rs.getString("name"),
                rs.getObject("date", LocalDate.class),
                mapTimeSlot(rs),
                mapTheme(rs),
                rs.getInt("waiting_number")
        );
    }

    private TimeSlot mapTimeSlot(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new TimeSlot(
                rs.getLong("t_id"),
                rs.getObject("start_at", LocalTime.class)
        );
    }

    private Theme mapTheme(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail_url")
        );
    }
}
