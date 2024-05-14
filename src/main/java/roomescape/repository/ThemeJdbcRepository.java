package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

@Repository
public class ThemeJdbcRepository implements ThemeRepository {

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> new Theme(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("thumbnail"));

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert themeInsert;

    public ThemeJdbcRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        BeanPropertySqlParameterSource themeParameters = new BeanPropertySqlParameterSource(theme);
        Long savedThemeId = themeInsert.executeAndReturnKey(themeParameters).longValue();
        return new Theme(savedThemeId, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        try {
            Theme theme = jdbcTemplate.queryForObject(sql, THEME_ROW_MAPPER, id);
            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }
}
