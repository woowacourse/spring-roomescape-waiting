package roomescape.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;

@Repository
@Transactional(readOnly = true)
public class TempThemeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public TempThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("theme").usingGeneratedKeyColumns("id");
    }

    public List<Theme> findTopThemes(Long count) {
        return jdbcTemplate.query("""
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
                """, (rs, rowNum) -> new Theme(rs.getString("name"), rs.getString("description"),
                rs.getString("url")), count);
    }
}
