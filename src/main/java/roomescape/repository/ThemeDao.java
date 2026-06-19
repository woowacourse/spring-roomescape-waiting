package roomescape.repository;

import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ThemeDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;
    private final RowMapper<Theme> rowMapper = (rs, rowNum) -> Theme.create(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("thumbnail_url"),
            rs.getString("description")
    );

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Theme save(Theme theme) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.name())
                .addValue("thumbnail_url", theme.thumbnailUrl())
                .addValue("description", theme.description());

        long themeId = insertExecutor.executeAndReturnKey(params)
                .longValue();
        return Theme.create(themeId, theme.name(), theme.thumbnailUrl(), theme.description());
    }

    public Optional<Theme> findById(long themeId) {
        String sql = """
                SELECT * FROM theme WHERE id = :id
                """;
        return jdbcTemplate.query(sql, Map.of("id", themeId), rowMapper)
                .stream()
                .findFirst();
    }

    public boolean existsById(long themeId) {
        String sql = """
                SELECT COUNT(*) FROM theme WHERE id = :id
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Map.of("id", themeId), Integer.class);
        return count != null && count > 0;
    }

    public List<Theme> findAll() {
        String sql = """
                SELECT * FROM theme
                """;
        return jdbcTemplate.query(sql, Map.of(), rowMapper);
    }

    public void deleteById(long themeId) {
        String sql = """
                DELETE FROM theme WHERE id = :id
                """;
        int affected = jdbcTemplate.update(sql, Map.of("id", themeId));

        if(affected == 0) {
            throw new ResourceNotFoundException("요청한 테마를 찾을 수 없습니다.");
        }
    }

    public List<Theme> findPopularBetween(LocalDate startAt, LocalDate endAt, int limit) {
        String sql = """
                SELECT 
                    theme.id, 
                    theme.name, 
                    theme.thumbnail_url, 
                    theme.description,
                    COUNT(reservation.id) as count
                FROM reservation
                INNER JOIN theme ON reservation.theme_id = theme.id
                WHERE reservation.date BETWEEN :startAt AND :endAt
                GROUP BY theme.id
                ORDER BY COUNT(reservation.id) DESC, theme.id ASC
                LIMIT :limit
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("startAt", startAt)
                .addValue("endAt", endAt)
                .addValue("limit", limit);
        return jdbcTemplate.query(sql, params, rowMapper);
    }
}
