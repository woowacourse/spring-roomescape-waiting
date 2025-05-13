package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.PopularThemeResponse;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (resultSet, rowNum) ->
            new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    private static final RowMapper<PopularThemeResponse> POPULAR_THEME_ROW_MAPPER = (resultSet, rowNum) ->
            new PopularThemeResponse(
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("theme")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "description", "thumbnail");
    }

    @Override
    public Theme save(final Theme theme) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail", theme.getThumbnail());
        final Long newId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Theme(newId, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public List<Theme> findAll() {
        final String sql = "select id, name, description, thumbnail from theme";
        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    @Override
    public List<PopularThemeResponse> findAllPopular() {
        final String sql = """
                    select t.name, t.description, t.thumbnail, count(t.name) as c 
                    from theme t
                    inner join reservation r
                    on r.theme_id = t.id
                    where r.date between
                            current_date - 7
                        and current_date -1
                    group by t.id, t.name, t.description, t.thumbnail
                    order by c desc
                    limit 10;
                """;
        return jdbcTemplate.query(sql, POPULAR_THEME_ROW_MAPPER);
    }

    @Override
    public int deleteById(final Long id) {
        final String sql = "delete from theme where id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        final String sql = "select id, name, description, thumbnail from theme where id = ?";
        return jdbcTemplate.query(sql, THEME_ROW_MAPPER, id)
                .stream()
                .findFirst();
    }
}
