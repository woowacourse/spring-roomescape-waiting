package roomescape.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;

@Repository
@Transactional(readOnly = true)
public class ThemeDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Theme findThemeById(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT id, name, description, url FROM theme WHERE id = ?",
                (rs, rowNum) -> new Theme(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("url")
                ),
                id
        );
    }

    public List<Theme> findAllThemes() {
        return jdbcTemplate.query(
                "SELECT id, name, description, url FROM theme",
                (rs, rowNum) -> new Theme(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("url")
                )
        );
    }

    public List<Theme> findTopThemes(Long count) {
        return jdbcTemplate.query(
                """
                           SELECT
                           t.id,
                           t.name,
                           t.description,
                           t.url,
                           COUNT(r.id) AS reservation_count
                           FROM theme t
                           INNER JOIN reservation r ON t.id = r.theme_id
                           WHERE r.date BETWEEN DATEADD('DAY', -7, CURRENT_DATE) AND DATEADD('DAY',-1,CURRENT_DATE)
                           GROUP BY t.id, t.name
                           ORDER BY reservation_count DESC
                           LIMIT ?;
                        """, (rs, rowNum) -> new Theme(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("url")
                ),
                count
        );
    }

    @Transactional
    public Theme save(Theme theme) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", theme.getName());
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

    @Transactional
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }
}
