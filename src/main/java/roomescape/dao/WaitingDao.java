package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.dto.RankedWaiting;
import roomescape.domain.Waiting;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class WaitingDao {

    private static final RowMapper<Waiting> ROW_MAPPER = (resultSet, rowNum) ->
            new Waiting(
                    resultSet.getLong("id"),
                    resultSet.getObject("created_at", LocalDateTime.class),
                    resultSet.getLong("slot_id"),
                    resultSet.getString("name")
            );

    private static final RowMapper<RankedWaiting> RANK_MAPPER = (resultSet, rowNum) ->
            new RankedWaiting(
                    resultSet.getLong("id"),
                    resultSet.getObject("created_at", LocalDateTime.class),
                    resultSet.getLong("slot_id"),
                    resultSet.getString("name"),
                    resultSet.getInt("rank"),
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getTime("start_at").toLocalTime(),
                    resultSet.getString("theme_name")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public WaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    public Waiting save(Waiting waiting) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("created_at", waiting.getCreatedAt());
        parameters.put("slot_id", waiting.getSlotId());
        parameters.put("name", waiting.getName());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);

        return waiting.createWithId(generatedId.longValue());
    }

    public Optional<Waiting> findById(long waitingId) {
        String sql = """
                SELECT id, 
                       created_at, 
                       slot_id,
                       name
                FROM waiting
                WHERE id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, waitingId));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    public List<RankedWaiting> findAllWithRankByName(String name) {
        String sql = """
                SELECT
                    w.id,
                    w.created_at,
                    w.slot_id,
                    w.name,
                    s.date,
                    rt.start_at,
                    t.name AS theme_name,
                    (
                        SELECT COUNT(*)
                        FROM waiting w2
                        WHERE w2.slot_id = w.slot_id
                          AND (w2.created_at < w.created_at OR (w2.created_at = w.created_at AND w2.id < w.id))
                    ) + 1 AS rank
                FROM waiting w
                JOIN slot s ON w.slot_id = s.id
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN theme t ON s.theme_id = t.id
                WHERE w.name = ?
                ORDER BY w.created_at, w.id;
                """;
        return jdbcTemplate.query(sql, RANK_MAPPER, name);
    }

    public boolean existsByCreatedAtAndSlotAndName(long slotId, String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM waiting
                    WHERE slot_id = ?
                        AND name = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, slotId, name));
    }

    public boolean existsByWaitingId(long waitingId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM waiting
                    WHERE id = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, waitingId));
    }

    public boolean existsByTheme(long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM waiting AS w
                    INNER JOIN slot AS s ON w.slot_id = s.id
                    WHERE s.theme_id = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, boolean.class, themeId));
    }

    public int delete(long waitingId) {
        String sql = """
                DELETE FROM waiting
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql, waitingId);
    }
}
