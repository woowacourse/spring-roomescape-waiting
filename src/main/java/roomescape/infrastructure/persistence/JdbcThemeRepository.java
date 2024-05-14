package roomescape.infrastructure.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.infrastructure.persistence.rowmapper.ThemeRowMapper;

@Repository
public class JdbcThemeRepository implements ThemeRepository {
    private static final String SQL = """
            select  id as theme_id, 
                    name as theme_name, 
                    description as theme_description, 
                    thumbnail as theme_thumbnail 
            from theme
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingColumns("name", "description", "thumbnail")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme create(Theme theme) {
        MapSqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail", theme.getThumbnailUrl());
        long id = jdbcInsert.executeAndReturnKey(parameter)
                .longValue();
        return theme.withId(id);
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query(SQL, ThemeRowMapper::mapRow);
    }

    @Override
    public Optional<Theme> findById(long id) {
        String sql = SQL + " where id = ?";
        try {
            Theme theme = jdbcTemplate.queryForObject(sql, ThemeRowMapper::mapRow, id);
            return Optional.of(Objects.requireNonNull(theme));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "delete from theme where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
