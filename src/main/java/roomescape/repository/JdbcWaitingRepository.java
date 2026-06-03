package roomescape.repository;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;
import roomescape.repository.mapper.DomainRowMapperFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private static final String BASE_SQL = """
            SELECT 
                w.id AS waiting_id, 
                w.name, 
                s.id AS slot_id, 
                s.date, 
                w.waiting_number, 
                t.id AS t_id, 
                t.start_at, 
                th.id AS theme_id, 
                th.name AS theme_name, 
                th.description AS theme_description, 
                th.thumbnail_url AS theme_thumbnail_url 
            FROM (
                SELECT id, name, slot_id, created_at, ROW_NUMBER() OVER (PARTITION BY slot_id ORDER BY created_at ASC, id ASC) AS waiting_number 
                FROM waiting
            ) w 
            INNER JOIN slot s ON w.slot_id = s.id 
            INNER JOIN time_slot t ON s.time_id = t.id 
            INNER JOIN theme th ON s.theme_id = th.id
            """;
    private static final String FIND_BY_NAME_SQL = BASE_SQL + " WHERE w.name = ?";
    private static final String FIND_BY_ID_SQL = BASE_SQL + " WHERE w.id = ?";
    private static final String EXISTS_SQL = "SELECT EXISTS (SELECT 1 FROM waiting WHERE name = ? AND slot_id = ?)";
    private static final String DELETE_SQL = "DELETE FROM waiting WHERE id = ?";
    private static final String CALCULATE_NUMBER_SQL = """
            SELECT COUNT(*) 
            FROM waiting w 
            WHERE w.slot_id = ? AND w.created_at <= (
                SELECT target.created_at FROM waiting target WHERE target.name = ? AND target.slot_id = ?
            )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Waiting> rowMapper;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate, DomainRowMapperFactory mapperFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = (rs, rowNum) -> new Waiting(
                rs.getLong("waiting_id"),
                rs.getString("name"),
                mapperFactory.mapSlot(rs),
                rs.getInt("waiting_number")
        );
    }

    @Override
    public int calculateWaitingNumber(Waiting waiting) {
        long slotId = waiting.getSlot().getId();
        return jdbcTemplate.queryForObject(CALCULATE_NUMBER_SQL, Integer.class, slotId, waiting.getName(), slotId);
    }

    @Override
    public Waiting save(Waiting waiting) {
        Map<String, Object> params = Map.of(
                "name", waiting.getName(),
                "slot_id", waiting.getSlot().getId()
        );
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Waiting(id, waiting.getName(), waiting.getSlot(), waiting.getWaitingNumber());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    @Override
    public boolean isExists(Waiting waiting) {
        return jdbcTemplate.queryForObject(EXISTS_SQL, Boolean.class, waiting.getName(), waiting.getSlot().getId());
    }

    @Override
    public List<Waiting> findByName(String name) {
        return jdbcTemplate.query(FIND_BY_NAME_SQL, rowMapper, name);
    }

    @Override
    public Optional<Waiting> findById(long waitingId) {
        List<Waiting> waitings = jdbcTemplate.query(FIND_BY_ID_SQL, rowMapper, waitingId);
        return Optional.ofNullable(DataAccessUtils.singleResult(waitings));
    }
}
