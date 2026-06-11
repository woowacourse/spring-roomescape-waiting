package roomescape.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.dto.AvailableTimeResponse;

@Repository
public class JdbcThemeRepository implements ThemeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Theme> themeRowMapper = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail_url")
    );

    private final RowMapper<AvailableTimeResponse> availableReservationTimeRowMapper =
            (rs, rowNum) -> new AvailableTimeResponse(
                    rs.getLong("id"),
                    rs.getObject("start_at", LocalTime.class),
                    rs.getBoolean("available"),
                    rs.getInt("waiting_count")
            );

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findPopularThemes(int size, LocalDate from, LocalDate to) {
        final String sql = """
                SELECT
                    th.id,
                    th.name,
                    th.description,
                    th.thumbnail_url
                FROM reservation_slot AS r
                INNER JOIN theme AS th ON r.theme_id = th.id
                WHERE r.date BETWEEN ? AND ?
                GROUP BY
                    th.id,
                    th.name,
                    th.description,
                    th.thumbnail_url
                ORDER BY COUNT(r.id) DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, themeRowMapper, from, to, size);
    }

    @Override
    public List<AvailableTimeResponse> findAvailableTimeById(long themeId, String date) {
        final String sql = """
                SELECT
                    rt.id,
                    rt.start_at,
                
                    CASE
                        WHEN COUNT(res.id) = 0 THEN TRUE
                        ELSE FALSE
                    END AS available,
                
                    GREATEST(COUNT(res.id) - 1, 0) AS waiting_count
                
                FROM reservation_time rt
                
                LEFT JOIN reservation_slot rs
                    ON rt.id = rs.time_id
                    AND rs.theme_id = ?
                    AND rs.date = ?
                
                LEFT JOIN reservation res
                    ON res.reservation_slot_id = rs.id
                    AND res.status != 'CANCELED'
                
                GROUP BY rt.id, rt.start_at
                ORDER BY rt.start_at
                """;
        return jdbcTemplate.query(sql, availableReservationTimeRowMapper, themeId, date);
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query("SELECT id, name, description, thumbnail_url FROM theme", themeRowMapper);
    }

    @Override
    public Long save(String name, String description, String thumbnailUrl) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "name", name,
                "description", description,
                "thumbnail_url", thumbnailUrl
        )).longValue();
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }
}
