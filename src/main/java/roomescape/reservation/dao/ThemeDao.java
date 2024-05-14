package roomescape.reservation.dao;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ThemeRepository;

@Repository
public class ThemeDao implements ThemeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Theme> rowMapper = (ResultSet resultSet, int rowNum) -> {
        return new Theme(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        );
    };

    private final ResultSetExtractor<Optional<Theme>> optionalResultSetExtractor = (ResultSet resultSet) -> {
        if (resultSet.next()) {
            Theme theme = new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );
            return Optional.of(theme);
        } else {
            return Optional.empty();
        }
    };

    public ThemeDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail FROM theme;";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(theme);
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public void deleteById(long themeId) {
        String sql = "DELETE FROM theme WHERE id = ?;";
        jdbcTemplate.update(sql, themeId);
    }

    @Override
    public Optional<Theme> findById(long themeId) {
        String sql = "SELECT id, name, description, thumbnail FROM theme WHERE id = ?;";
        return jdbcTemplate.query(sql, optionalResultSetExtractor, themeId);
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
                SELECT theme.id, theme.name, theme.description, theme.thumbnail, COUNT(*) AS reservation_count 
                FROM theme 
                INNER JOIN reservation AS re ON re.theme_id = theme.id 
                INNER JOIN member_reservation AS mr ON mr.reservation_id = re.id 
                WHERE re.date BETWEEN ? AND ? 
                GROUP BY theme.id, theme.name 
                ORDER BY reservation_count DESC 
                LIMIT ?;
                """;

        return jdbcTemplate.query(sql, rowMapper, startDate, endDate, limit);
    }

}
