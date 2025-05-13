package roomescape.infrastructure.persistance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final RowMapper<Theme> themeRowMapper = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail")
    );

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, DataSource dataSource) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long create(Theme theme) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(theme);
        return simpleJdbcInsert.executeAndReturnKey(params).longValue();
    }

    @Override
    public Optional<Theme> findById(Long themeId) {
        String sql = """
                SELECT id, name, description, thumbnail
                FROM theme
                WHERE id = :id
                """;
        try {
            Theme theme = namedParameterJdbcTemplate.queryForObject(sql, Map.of("id", themeId), themeRowMapper);
            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findAll() {
        String sql = """
                SELECT id, name, description, thumbnail
                FROM theme
                """;
        return namedParameterJdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public void deleteById(Long themeId) {
        String sql = """
                DELETE FROM theme
                WHERE id = :id
                """;
        namedParameterJdbcTemplate.update(sql, Map.of("id", themeId));
    }

    @Override
    public List<Theme> findRankBetweenDate(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail
                FROM theme t
                LEFT JOIN reservation r
                    ON t.id = r.theme_id AND r.date BETWEEN :startDate AND :endDate
                GROUP BY t.id
                ORDER BY COUNT(r.id) DESC
                LIMIT :limit
                """;
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("limit", limit);
        return namedParameterJdbcTemplate.query(sql, param, themeRowMapper);
    }
}
