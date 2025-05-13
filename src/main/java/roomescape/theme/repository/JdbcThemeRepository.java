package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.theme.entity.Theme;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Theme> rowMapper = (resultSet, rowNum) -> new Theme(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail")
    );

    @Override
    public Theme save(Theme theme) {
        String sql = """
                INSERT INTO theme (name, description, thumbnail)
                VALUES (:name, :description, :thumbnail)
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail", theme.getThumbnail());

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        return new Theme(
                keyHolder.getKey().longValue(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Theme> findPopularDescendingUpTo(
            LocalDate startDate,
            LocalDate endDate,
            int limit
    ) {
        String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail
                FROM theme t
                LEFT JOIN (
                    SELECT
                        theme_id,
                        COUNT(*) as cnt
                    FROM reservation
                    WHERE date BETWEEN :startDate AND :endDate
                    GROUP BY theme_id
                ) r_stats ON t.id = r_stats.theme_id
                ORDER BY cnt DESC, theme_id DESC
                FETCH FIRST :limit ROWS ONLY
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        try {
            Theme theme = jdbcTemplate.queryForObject(sql, params, rowMapper);
            return Optional.of(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Theme> findByName(String name) {
        String sql = "SELECT * FROM theme WHERE name = :name";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name);

        try {
            Theme theme = jdbcTemplate.queryForObject(sql, params, rowMapper);
            return Optional.of(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM theme WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        int updated = jdbcTemplate.update(sql, params);
        return updated > 0;
    }
}
