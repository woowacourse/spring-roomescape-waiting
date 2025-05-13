package roomescape.repository.theme;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

@Repository
public class ThemeJdbcRepository implements ThemeRepository {

    private static final RowMapper<Theme> ROW_MAPPER = (resultSet, rowNumber) ->
            new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ThemeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme add(Theme theme) {
        Map<String, String> params = new HashMap<>();
        params.put("name", theme.getName());
        params.put("description", theme.getDescription());
        params.put("thumbnail", theme.getThumbnail());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return theme.withId(id);
    }

    @Override
    public List<Theme> findAll() {
        String sql = "select * from theme";
        List<Theme> themes = jdbcTemplate.query(
                sql,
                ROW_MAPPER
        );
        return themes;
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE from theme where id=?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "select * from theme where id=?";
        List<Theme> theme = jdbcTemplate.query(
                sql,
                ROW_MAPPER,
                id
        );
        return theme.stream().findFirst();
    }
}
