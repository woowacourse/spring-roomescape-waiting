package roomescape.repository.jdbc;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.entity.Theme;
import roomescape.repository.ThemeRepository;

public class JdbcThemeRepository implements ThemeRepository {

    private static final String DEFAULT_SELECT_SQL = "select id, name, description, thumbnail from theme";

    private static final RowMapper<Theme> themeRowMapper = (resultSet, rowNumber) -> {
        long id = resultSet.getLong("id");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        String thumbnail = resultSet.getString("thumbnail");

        return new Theme(id, name, description, thumbnail);
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Theme save(Theme theme) {
        String sql = "insert into theme (name, description, thumbnail) values(?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnail());
            return ps;
        }, keyHolder);

        long generatedId = keyHolder.getKey().longValue();

        return new Theme(
                generatedId,
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query(DEFAULT_SELECT_SQL, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = DEFAULT_SELECT_SQL + " where id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, themeRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsReservationByThemeId(long id) {
        String sql = """
                select count(t.id) from theme as t
                inner join reservation as r
                on t.id = r.time_id
                where t.id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "delete from theme where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
