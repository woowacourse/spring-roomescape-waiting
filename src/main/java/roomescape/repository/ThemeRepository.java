package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.PopularTheme;
import roomescape.domain.PopularThemeCondition;
import roomescape.domain.Theme;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) -> new Theme(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail")
    );

    private final RowMapper<PopularTheme> popularThemeRowMapper = (resultSet, rowNum) -> new PopularTheme(
            themeRowMapper.mapRow(resultSet, rowNum),
            resultSet.getLong("reservation_count")
    );


    public ThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail FROM theme;";
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    public Optional<Theme> findById(Long id) {
        String sql = "SELECT id, name, description, thumbnail FROM theme WHERE id = ?;";
        List<Theme> result = jdbcTemplate.query(sql, themeRowMapper, id);
        return result.stream().findAny();
    }

    public Theme insert(Theme theme) {
        String sql = "INSERT INTO theme(name, description, thumbnail) VALUES (?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(
                    sql,
                    new String[]{"id"});
            pstmt.setString(1, theme.getName());
            pstmt.setString(2, theme.getDescription());
            pstmt.setString(3, theme.getThumbnail());
            return pstmt;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return theme.withId(id);
    }

    public int delete(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?;";
        return jdbcTemplate.update(sql, id);
    }

    public boolean existsById(Long id) {
        String sql = "SELECT count(*) FROM theme WHERE id = ?;";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public List<PopularTheme> findPopular(PopularThemeCondition condition) {
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
        return jdbcTemplate.query(
                sql,
                popularThemeRowMapper,
                condition.getStartDate(),
                condition.getEndDate(),
                condition.getLimit()
        );
    }
}
