package roomescape.infrastructure.jdbc;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcThemes implements Themes {

    private static final RowMapper<Theme> ROW_MAPPER = (resultSet, rowNum) -> Theme.restore(
            resultSet.getString("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insert;

    public JdbcThemes(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insert = new SimpleJdbcInsert(jdbcTemplate).
                withTableName("theme");
    }

    @Override
    public void save(Theme theme) {
        insert.execute(Map.of(
                "id", theme.getId().value(),
                "name", theme.getName().value(),
                "description", theme.getDescription(),
                "thumbnail", theme.getThumbnail()
        ));
    }

    @Override
    public List<Theme> findAll() {
        final String sql = """
                SELECT * FROM theme
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startInclusive, LocalDate endInclusive, int count) {
        final String sql = """
                SELECT t.*
                FROM (
                    SELECT r.theme_id, COUNT(*) AS reservation_count
                    FROM reservation r
                    WHERE r.date BETWEEN ? AND ?
                    GROUP BY r.theme_id
                    ORDER BY reservation_count DESC
                    LIMIT ?
                ) AS ranked
                JOIN theme t ON ranked.theme_id = t.id;
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER, startInclusive, endInclusive, count);
    }

    @Override
    public Optional<Theme> findById(final Id themeId) {
        final String sql = """
                SELECT * FROM theme
                WHERE id = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, ROW_MAPPER, themeId.value()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existById(final Id themeId) {
        final String sql = """
                SELECT COUNT(*) FROM theme
                WHERE id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId.value());
        return count != null && count > 0;
    }

    @Override
    public void deleteById(final Id themeId) {
        final String sql = """
                DELETE FROM theme
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, themeId.value());
    }
}
