package roomescape.repository;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.mapper.DomainRowMapperFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final String FIND_ALL_SQL = """
            SELECT 
                id AS theme_id, 
                name AS theme_name, 
                description AS theme_description, 
                thumbnail_url AS theme_thumbnail_url 
            FROM theme
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + " WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM theme WHERE id = ?";
    private static final String UPDATE_SQL = "UPDATE theme SET name = ?, description = ?, thumbnail_url = ? WHERE id = ?";
    private static final String FIND_POPULAR_SQL = """
            SELECT
                t.id AS theme_id,
                t.name AS theme_name,
                t.description AS theme_description,
                t.thumbnail_url AS theme_thumbnail_url,
                COUNT(r.id) AS reservation_count
            FROM session s
            INNER JOIN reservation r ON s.id = r.session_id
            INNER JOIN theme t ON s.theme_id = t.id
            WHERE s.date BETWEEN ? AND ?
            GROUP BY t.id
            ORDER BY reservation_count DESC
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Theme> rowMapper;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate, DomainRowMapperFactory mapperFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = (rs, rowNum) -> mapperFactory.mapTheme(rs);
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, rowMapper);
    }

    @Override
    public Optional<Theme> findById(long id) {
        List<Theme> themes = jdbcTemplate.query(FIND_BY_ID_SQL, rowMapper, id);
        return Optional.ofNullable(DataAccessUtils.singleResult(themes));
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> params = Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail_url", theme.getThumbnailUrl()
        );
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
    }

    @Override
    public void deleteById(long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    @Override
    public Theme update(Theme theme) {
        int columns = jdbcTemplate.update(UPDATE_SQL, theme.getName(), theme.getDescription(), theme.getThumbnailUrl(), theme.getId());
        checkUpdateResult(columns, theme.getId());
        return theme;
    }

    @Override
    public List<Theme> findPopularThemes(Long topCount, LocalDate fromDate, LocalDate toDate) {
        return jdbcTemplate.query(FIND_POPULAR_SQL, rowMapper, fromDate, toDate, topCount);
    }

    private void checkUpdateResult(int columns, long id) {
        if (columns == 0) {
            throw new ThemeNotFoundException(id);
        }
    }
}
