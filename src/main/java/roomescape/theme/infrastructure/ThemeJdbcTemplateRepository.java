package roomescape.theme.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Repository
public class ThemeJdbcTemplateRepository implements ThemeRepository {

    private static final String FIND_QUERY = "SELECT id, name, description, thumbnail_url FROM theme WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT id, name, description, thumbnail_url FROM theme";
    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> Theme.createRow(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail_url")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ThemeJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> params = prepareInsertParams(theme);
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return theme.appendId(id);
    }

    private Map<String, Object> prepareInsertParams(Theme theme) {
        return Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail_url", theme.getThumbnailUrl()

        );
    }

    @Override
    public Optional<Theme> findById(Long id) {
        List<Theme> findTheme = jdbcTemplate.query(FIND_QUERY,
                THEME_ROW_MAPPER,
                id
        );
        return findTheme.stream()
                .findFirst();
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, THEME_ROW_MAPPER);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }
}
