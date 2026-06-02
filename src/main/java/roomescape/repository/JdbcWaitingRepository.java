package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.service.dto.WaitingWithNumber;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
                waiting.getCreatedAt());
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
    public boolean exists(String name, LocalDate date, Long timeId, Long themeId) {
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

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, name, date, timeId, themeId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public List<WaitingWithNumber> findByName(String name) {
        String sql = """
                SELECT *
                FROM (
                    SELECT w.id,
                           w.name,
                           w.date,
                           w.created_at,
                           ts.id AS time_id,
                           ts.start_at,
                           th.id AS theme_id,
                           th.name AS theme_name,
                           th.description AS theme_description,
                           th.thumbnail_url AS theme_thumbnail_url,
                           ROW_NUMBER() OVER (
                               PARTITION BY w.date, w.time_id, w.theme_id
                               ORDER BY w.created_at ASC, w.id ASC
                           ) AS waiting_number
                    FROM waiting w
                    INNER JOIN time_slot ts ON w.time_id = ts.id
                    INNER JOIN theme th ON w.theme_id = th.id
                ) ranked
                WHERE ranked.name = ?
                """;

        return jdbcTemplate.query(sql, waitingWithNumberRowMapper(), name);
    }

    @Override
    public Optional<Waiting> findById(long waitingId) {
        String sql = """
                SELECT w.id,
                       w.name,
                       w.date,
                       w.created_at,
                       ts.id AS time_id,
                       ts.start_at,
                       th.id AS theme_id,
                       th.name AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail_url AS theme_thumbnail_url
                FROM waiting w
                INNER JOIN time_slot ts ON w.time_id = ts.id
                INNER JOIN theme th ON w.theme_id = th.id
                WHERE w.id = ?
                """;

        List<Waiting> waitings = jdbcTemplate.query(sql, waitingRowMapper(), waitingId);
        return Optional.ofNullable(DataAccessUtils.singleResult(waitings));
    }

    @Override
    public Optional<WaitingWithNumber> findWaitingWithNumberById(long id) {
        String sql = """
                SELECT *
                FROM (
                    SELECT w.id,
                           w.name,
                           w.date,
                           w.created_at,
                           ts.id AS time_id,
                           ts.start_at,
                           th.id AS theme_id,
                           th.name AS theme_name,
                           th.description AS theme_description,
                           th.thumbnail_url AS theme_thumbnail_url,
                           ROW_NUMBER() OVER (
                               PARTITION BY w.date, w.time_id, w.theme_id
                               ORDER BY w.created_at ASC, w.id ASC
                           ) AS waiting_number
                    FROM waiting w
                    INNER JOIN time_slot ts ON w.time_id = ts.id
                    INNER JOIN theme th ON w.theme_id = th.id
                ) ranked
                WHERE ranked.id = ?
                """;

        List<WaitingWithNumber> waitings = jdbcTemplate.query(sql, waitingWithNumberRowMapper(), id);
        return Optional.ofNullable(DataAccessUtils.singleResult(waitings));
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingColumns("name", "date", "time_id", "theme_id", "created_at")
                .usingGeneratedKeyColumns("id");
    }

    private Map<String, Object> createParams(Waiting waiting) {
        return Map.of(
                "name", waiting.getName(),
                "date", waiting.getDate(),
                "time_id", waiting.getTimeSlot().getId(),
                "theme_id", waiting.getTheme().getId(),
                "created_at", waiting.getCreatedAt()
        );
    }

    private RowMapper<WaitingWithNumber> waitingWithNumberRowMapper() {
        return (rs, rowNum) -> new WaitingWithNumber(
                waitingRowMapper().mapRow(rs, rowNum),
                rs.getInt("waiting_number")
        );
    }

    private RowMapper<Waiting> waitingRowMapper() {
        return (rs, rowNum) -> new Waiting(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getObject("date", LocalDate.class),
                new TimeSlot(
                        rs.getLong("time_id"),
                        rs.getObject("start_at", LocalTime.class)
                ),
                new Theme(
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("theme_description"),
                        rs.getString("theme_thumbnail_url")
                ),
                rs.getObject("created_at", LocalDateTime.class)
        );
    }
}
