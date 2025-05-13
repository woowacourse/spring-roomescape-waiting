package roomescape.infrastructure.jdbc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> Theme.of(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail")
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long save(Theme theme) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> params = Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail", theme.getThumbnail()
        );

        Number key = simpleJdbcInsert.executeAndReturnKey(params);
        return key.longValue();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "select id, name, description, thumbnail from theme where id = ?";
        try {
            Theme theme = jdbcTemplate.queryForObject(sql, THEME_ROW_MAPPER, id);
            return Optional.of(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail FROM theme";
        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public List<Theme> findThemeRanking(int count, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT th.id, th.name, th.description, th.thumbnail, count(r.id)
                FROM theme as th
                LEFT JOIN reservation as r on th.id = r.theme_id AND r.date BETWEEN ? and ?
                GROUP BY th.id, th.name, th.description, th.thumbnail
                ORDER BY COUNT(r.id) desc
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, THEME_ROW_MAPPER, startDate, endDate, count);
    }
}
