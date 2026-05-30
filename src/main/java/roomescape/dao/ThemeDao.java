package roomescape.dao;

import static roomescape.dao.rowmapper.ThemeMapper.THEME_ROW_MAPPER;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.slot.theme.Theme;

@Repository
public class ThemeDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Theme save(Theme theme) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", theme.getName().value());
        params.put("description", theme.getDescription());
        params.put("url", theme.getUrl());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new Theme(
                id,
                theme.getName(),
                theme.getDescription(),
                theme.getUrl()
        );
    }

    public Optional<Theme> findThemeById(Long id) {
        String sql = """
                SELECT id, name, description, url 
                FROM theme 
                WHERE id = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        THEME_ROW_MAPPER,
                        id
                ).stream()
                .findFirst();
    }

    public List<Theme> findAllThemes() {
        return jdbcTemplate.query(
                "SELECT id, name, description, url FROM theme",
                THEME_ROW_MAPPER
        );
    }

    public List<Theme> findPopularThemes(int limit, LocalDate today) {
        return jdbcTemplate.query(
                """
                           SELECT
                           t.id, t.name, t.description, t.url, r.reservation_count
                           FROM theme t
                           INNER JOIN (
                               SELECT theme_id, COUNT(id) AS reservation_count
                               FROM reservation
                               WHERE date >= ? AND date < ?
                               GROUP BY theme_id
                           ) r ON t.id = r.theme_id
                           ORDER BY r.reservation_count DESC
                           LIMIT ?;
                        """,
                THEME_ROW_MAPPER,
                today.minusDays(7),
                today,
                limit
        );
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }
}
