package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Repository
public class WaitingJdbcTemplateRepository implements WaitingRepository {

    private static final String FIND_BY_ID_QUERY = """
        SELECT *
        FROM (
            SELECT w.id,
                   w.name AS waiting_name,
                   w.date,
                   w.created_at,
                   rt.id AS time_id,
                   rt.start_at,
                   t.id AS theme_id,
                   t.name AS theme_name,
                   t.description AS theme_description,
                   t.thumbnail_url,
                   ROW_NUMBER() OVER (
                       PARTITION BY w.date, w.time_id, w.theme_id
                       ORDER BY w.created_at, w.id
                   ) AS rank
            FROM waiting w
            JOIN reservation_time rt ON w.time_id = rt.id
            JOIN theme t ON w.theme_id = t.id
        ) ranked_waiting
        WHERE id = ?
        """;
    private static final String FIND_BY_NAME_QUERY = """
        SELECT *
        FROM (
            SELECT w.id,
                   w.name AS waiting_name,
                   w.date,
                   w.created_at,
                   rt.id AS time_id,
                   rt.start_at,
                   t.id AS theme_id,
                   t.name AS theme_name,
                   t.description AS theme_description,
                   t.thumbnail_url,
                   ROW_NUMBER() OVER (
                       PARTITION BY w.date, w.time_id, w.theme_id
                       ORDER BY w.created_at, w.id
                   ) AS rank
            FROM waiting w
            JOIN reservation_time rt ON w.time_id = rt.id
            JOIN theme t ON w.theme_id = t.id
        ) ranked_waiting
        WHERE waiting_name = ?
        """;
    private static final String FIND_BY_DATE_AND_TIME_ID_AND_THEME_ID_QUERY = """
        SELECT *
        FROM (
            SELECT w.id,
                   w.name AS waiting_name,
                   w.date,
                   w.created_at,
                   rt.id AS time_id,
                   rt.start_at,
                   t.id AS theme_id,
                   t.name AS theme_name,
                   t.description AS theme_description,
                   t.thumbnail_url,
                   ROW_NUMBER() OVER (
                       PARTITION BY w.date, w.time_id, w.theme_id
                       ORDER BY w.created_at, w.id
                   ) AS rank
            FROM waiting w
            JOIN reservation_time rt ON w.time_id = rt.id
            JOIN theme t ON w.theme_id = t.id
        ) ranked_waiting
        WHERE date = ?
          AND time_id = ?
          AND theme_id = ?
        """;
    private static final String DELETE_BY_ID_AND_NAME_QUERY = "DELETE FROM waiting WHERE id = ? AND name = ?";

    private static final RowMapper<Waiting> ROW_MAPPER = (rs, rowNum) -> {
        ReservationTime time = ReservationTime.createRow(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime()
        );

        Theme theme = Theme.createRow(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("thumbnail_url")
        );

        return Waiting.createRow(
                rs.getLong("id"),
                rs.getString("waiting_name"),
                rs.getDate("date").toLocalDate(),
                time,
                theme,
                rs.getLong("rank"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public WaitingJdbcTemplateRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }


    @Override
    public Waiting save(Waiting waiting) {
        Map<String, Object> params = Map.of(
                "name", waiting.getName(),
                "date", waiting.getDate(),
                "time_id", waiting.getTime().getId(),
                "theme_id", waiting.getTheme().getId(),
                "created_at", waiting.getCreatedAt()
        );
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return waiting.appendId(id);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        List<Waiting> waiting = jdbcTemplate.query(
                FIND_BY_ID_QUERY,
                ROW_MAPPER,
                id
        );
        return waiting.stream()
                .findFirst();
    }

    @Override
    public Optional<Waiting> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        List<Waiting> waiting = jdbcTemplate.query(
                FIND_BY_DATE_AND_TIME_ID_AND_THEME_ID_QUERY,
                ROW_MAPPER,
                date,
                timeId,
                themeId
        );
        return waiting.stream()
                .findFirst();
    }

    @Override
    public void deleteByIdAndName(Long id, String name) {
        jdbcTemplate.update(
                DELETE_BY_ID_AND_NAME_QUERY,
                id,
                name
        );
    }

    @Override
    public List<Waiting> findByName(String name) {
        return jdbcTemplate.query(
                FIND_BY_NAME_QUERY,
                ROW_MAPPER,
                name
        );
    }
}
