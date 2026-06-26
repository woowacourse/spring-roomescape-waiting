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
import roomescape.domain.Session;
import roomescape.repository.mapper.DomainRowMapperFactory;

@Repository
public class JdbcSessionRepository implements SessionRepository {

    private static final String BASE_SQL = """
            SELECT
                s.id AS session_id,
                s.date,
                t.id AS t_id,
                t.start_at,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail_url
            FROM session s
            INNER JOIN time_slot t ON s.time_id = t.id
            INNER JOIN theme th ON s.theme_id = th.id
            """;
    private static final String FIND_ALL_SQL = BASE_SQL;
    private static final String FIND_BY_ID_SQL = BASE_SQL + " WHERE s.id = ?";
    private static final String FIND_BY_CONDITIONS_SQL = BASE_SQL + " WHERE s.date = ? AND s.time_id = ? AND s.theme_id = ?";
    private static final String DELETE_SQL = "DELETE FROM session WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Session> rowMapper;

    public JdbcSessionRepository(JdbcTemplate jdbcTemplate, DomainRowMapperFactory mapperFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("session")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = (rs, rowNum) -> mapperFactory.mapSession(rs);
    }

    @Override
    public List<Session> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, rowMapper);
    }

    @Override
    public Optional<Session> findById(long id) {
        List<Session> sessions = jdbcTemplate.query(FIND_BY_ID_SQL, rowMapper, id);
        return Optional.ofNullable(DataAccessUtils.singleResult(sessions));
    }

    @Override
    public Session save(Session session) {
        Map<String, Object> params = Map.of(
                "date", session.getDate(),
                "time_id", session.getTimeSlot().getId(),
                "theme_id", session.getTheme().getId()
        );
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Session(id, session.getDate(), session.getTimeSlot(), session.getTheme());
    }

    @Override
    public Optional<Session> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        List<Session> sessions = jdbcTemplate.query(FIND_BY_CONDITIONS_SQL, rowMapper, date, timeId, themeId);
        return Optional.ofNullable(DataAccessUtils.singleResult(sessions));
    }

    @Override
    public void deleteById(long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }
}
