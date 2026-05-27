package roomescape.dao;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
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
                DELETE FROM theme
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql, waitingId);
    }
}
