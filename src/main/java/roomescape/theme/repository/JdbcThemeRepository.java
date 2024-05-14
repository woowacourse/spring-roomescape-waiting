package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.model.Theme;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Theme> themeRowMapper =
            (resultSet, rowNum) -> new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    @Override
    public Theme save(final Theme theme) {
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail", theme.getThumbnail());

        long id = simpleJdbcInsert.executeAndReturnKey(source).longValue();

        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public List<Theme> findAll() {
        String sql = "select id, name, description, thumbnail from theme";
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        String sql = """
                select id, name, description, thumbnail 
                from theme 
                where id = ? 
                limit 1
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, themeRowMapper, id));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findOrderByReservation(int size) {
        String sql = """
                select t.id, t.name, t.description, t.thumbnail, count(t.id) as count
                from theme as t
                left join reservation as r
                on r.theme_id = t.id
                group by t.id
                order by count desc
                limit ? 
                """;
        return jdbcTemplate.query(sql, themeRowMapper, size);
    }

    @Override
    public boolean existsById(final Long id) {
        String sql = """
                select exists ( select 1 
                from theme as t
                where t.id = ? )
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, id) != 0;
    }

    @Override
    public void deleteById(final Long id) {
        String sql = "delete from theme where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
