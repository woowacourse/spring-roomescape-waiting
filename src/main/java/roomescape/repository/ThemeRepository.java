package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.repository.result.PopularThemeResult;

@Repository
public class ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public ThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PopularThemeResult> findPopular(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail,
                    COUNT(r.id) AS reservation_count
                FROM theme AS t
                LEFT JOIN reservation AS r
                    ON r.theme_id = t.id
                    AND r.date >= ?
                    AND r.date <= ?
                GROUP BY t.id, t.name, t.description, t.thumbnail
                ORDER BY reservation_count DESC, t.id ASC
                LIMIT ?;
                """;
        return jdbcTemplate.query(sql, popularThemeRowMapper, startDate, endDate, limit);
    }

    private final RowMapper<PopularThemeResult> popularThemeRowMapper = (resultSet, rowNum) -> {
        PopularThemeResult theme = new PopularThemeResult(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail"),
                resultSet.getLong("reservation_count"));
        return theme;
    };
}
