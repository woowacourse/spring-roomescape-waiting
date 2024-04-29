package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final RowMapper<Theme> ROW_MAPPER = ((rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail")
    ));

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("THEME")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query("SELECT id, name, description, thumbnail FROM theme", ROW_MAPPER);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "SELECT id, name, description, thumbnail FROM theme WHERE id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findByPeriodOrderByReservationCount(LocalDate start, LocalDate end, int count) {
        String sql = """
                SELECT 
                    th.id,
                    th.name, 
                    th.description, 
                    th.thumbnail, 
                    COUNT(r.id) AS reservation_count
                FROM theme AS th
                INNER JOIN reservation AS r ON th.id = r.theme_id
                WHERE r.date BETWEEN ? AND ?
                GROUP BY th.id
                ORDER BY reservation_count DESC 
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, start, end, count);
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(theme);
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return new Theme(id, theme);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }
}
