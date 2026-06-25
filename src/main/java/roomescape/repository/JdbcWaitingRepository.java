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
                s.id AS session_id,
                s.date,
                w.waiting_number,
                t.id AS t_id,
                t.start_at,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail_url
            FROM (
                SELECT id, name, session_id, created_at, ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY created_at ASC, id ASC) AS waiting_number
                FROM waiting
            ) w
            INNER JOIN session s ON w.session_id = s.id
            INNER JOIN time_slot t ON s.time_id = t.id
            INNER JOIN theme th ON s.theme_id = th.id
            """;
    private static final String FIND_BY_NAME_SQL = BASE_SQL + " WHERE w.name = ?";
    private static final String FIND_BY_ID_SQL = BASE_SQL + " WHERE w.id = ?";
    private static final String FIND_FIRST_BY_SESSION_SQL = BASE_SQL + " WHERE s.id = ? ORDER BY w.waiting_number ASC LIMIT 1";
    private static final String FIND_ALL_BY_SESSION_SQL = BASE_SQL + " WHERE s.id = ? ORDER BY w.waiting_number ASC";
    private static final String EXISTS_SQL = "SELECT EXISTS (SELECT 1 FROM waiting WHERE name = ? AND session_id = ?)";
    private static final String EXISTS_BY_SESSION_SQL = "SELECT EXISTS (SELECT 1 FROM waiting WHERE session_id = ?)";
    private static final String DELETE_SQL = "DELETE FROM waiting WHERE id = ?";
    private static final String CALCULATE_NUMBER_SQL = """
            SELECT COUNT(*)
            FROM waiting w
            WHERE w.session_id = ? AND w.created_at <= (
                SELECT target.created_at FROM waiting target WHERE target.name = ? AND target.session_id = ?
            )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Waiting> rowMapper;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate, DomainRowMapperFactory mapperFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "session_id");
        this.rowMapper = (rs, rowNum) -> new Waiting(
                rs.getLong("waiting_id"),
                rs.getString("name"),
                mapperFactory.mapSession(rs),
                rs.getInt("waiting_number")
        );
    }

    @Override
    public int calculateWaitingNumber(Waiting waiting) {
        long sessionId = waiting.getSession().getId();
        return jdbcTemplate.queryForObject(CALCULATE_NUMBER_SQL, Integer.class, sessionId, waiting.getName(), sessionId);
    }

    @Override
    public Waiting save(Waiting waiting) {
        Map<String, Object> params = Map.of(
                "name", waiting.getName(),
                "session_id", waiting.getSession().getId()
        );
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Waiting(id, waiting.getName(), waiting.getSession(), waiting.getWaitingNumber());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    @Override
    public boolean isExists(Waiting waiting) {
        return jdbcTemplate.queryForObject(EXISTS_SQL, Boolean.class, waiting.getName(), waiting.getSession().getId());
    }

    @Override
    public boolean isExistsBySessionId(long sessionId) {
        return jdbcTemplate.queryForObject(EXISTS_BY_SESSION_SQL, Boolean.class, sessionId);
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

    @Override
    public Waiting findFirstBySessionId(long sessionId) {
        List<Waiting> waitings = jdbcTemplate.query(FIND_FIRST_BY_SESSION_SQL, rowMapper, sessionId);
        return DataAccessUtils.singleResult(waitings);
    }

    @Override
    public List<Waiting> findAllBySessionId(long sessionId) {
        return jdbcTemplate.query(FIND_ALL_BY_SESSION_SQL, rowMapper, sessionId);
    }
}
