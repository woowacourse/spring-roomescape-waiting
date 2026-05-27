package roomescape.dao;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.dto.RankedWaiting;
import roomescape.domain.Waiting;

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
                    resultSet.getInt("rank")
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

    public List<RankedWaiting> findAllWithRankByName(String name) {
        String sql = """
                SELECT
                    w.id,
                    w.created_at,
                    w.slot_id,
                    w.name,
                    (
                        SELECT COUNT(*)
                        FROM waiting w2
                        WHERE w2.slot_id = w.slot_id
                          AND w2.created_at < w.created_at
                    ) + 1 AS rank
                FROM waiting w
                WHERE w.name = ?
                ORDER BY w.created_at;
                """;
        return jdbcTemplate.query(sql, RANK_MAPPER, name);
    }

    public boolean existsByCreatedAtAndSlotAndName(LocalDateTime createdAt, long slotId, String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM waiting
                    WHERE created_at = ?
                        AND slot_id = ?
                        AND name = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, createdAt, slotId, name));
    }

    public int delete(long waitingId) {
        String sql = """
                DELETE FROM waiting
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql, waitingId);
    }
}
