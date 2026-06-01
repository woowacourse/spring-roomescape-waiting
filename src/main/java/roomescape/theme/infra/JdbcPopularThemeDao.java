package roomescape.theme.infra;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.theme.application.dto.PopularThemeResult;
import roomescape.theme.application.dao.PopularThemeDao;
import roomescape.theme.domain.PopularThemePeriod;

@RequiredArgsConstructor
@Repository
public class JdbcPopularThemeDao implements PopularThemeDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<PopularThemeResult> findTop10PopularThemes(PopularThemePeriod period) {
        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail_img_url, COUNT(*) as reserved_count
                FROM theme t
                JOIN reservation r ON t.id = r.theme_id
                WHERE r.date BETWEEN ? AND ?
                GROUP BY t.id, t.name, t.description, t.thumbnail_img_url
                ORDER BY reserved_count DESC
                LIMIT 10
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new PopularThemeResult(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("thumbnail_img_url"),
                        rs.getInt("reserved_count")
                ), period.from(), period.to());
    }
}
