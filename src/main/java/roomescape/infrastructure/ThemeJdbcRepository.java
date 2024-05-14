package roomescape.infrastructure;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ThemeJdbcRepository implements ThemeRepository {
    private static final String TABLE_NAME = "theme";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Theme> rowMapper = (resultSet, rowNum) -> {
        return new Theme(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        );
    };

    public ThemeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, String> params = Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail", theme.getThumbnail()
        );
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Theme(id, theme);
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM theme WHERE name = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, name) > 0;
    }

    @Override
    public Optional<Theme> findById(long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findByReservationTermAndCount(String startDate, String endDate, long count) {
        String sql = "SELECT id, name, description, thumbnail FROM theme "
                + "LEFT JOIN "
                + "(SELECT theme_id, COUNT(theme_id) as count FROM reservation "
                + "WHERE date BETWEEN ? AND ? "
                + "GROUP BY theme_id) "
                + "WHERE theme_id = id "
                + "ORDER BY count DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, startDate, endDate, count);
    }
}
