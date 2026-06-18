package roomescape.domain.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.error.type.ThemeErrorType;
import roomescape.global.error.exception.GeneralException;

public class JdbcThemeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("theme")
            .usingColumns("name", "description", "image_url")
            .usingGeneratedKeyColumns("id");
    }

    public List<Theme> findAllByDeletedAtIsNull() {
        String sql = "SELECT id, name, description, image_url FROM theme WHERE deleted_at IS NULL";
        return jdbcTemplate.query(
            sql,
            (resultSet, rowNum) -> Theme.reconstruct(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("image_url"),
                null
            ));
    }

    public Theme save(Theme theme) {
        Map<String, Object> args = Map.of(
            "name", theme.getName(),
            "description", theme.getDescription(),
            "image_url", theme.getImageUrl()
        );
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(args).longValue();
        return Theme.reconstruct(generatedKey, theme.getName(), theme.getDescription(), theme.getImageUrl(), null);
    }

    public void deleteThemeById(Long id) {
        final String sql = "UPDATE theme SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id AND deleted_at IS NULL";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);

        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            throw new GeneralException(ThemeErrorType.THEME_NOT_FOUND);
        }
    }


    public Optional<Theme> findThemeByIdAndDeletedAtIsNull(Long id) {
        final String sql = "SELECT id, name, description, image_url FROM theme WHERE id = :id AND deleted_at IS NULL";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        try {
            Theme theme = jdbcTemplate.queryForObject(
                sql,
                parameters,
                (resultSet, rowNum) -> Theme.reconstruct(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("image_url"),
                    null
                )
            );
            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public boolean existsThemeByIdAndDeletedAtIsNull(Long id) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM theme
                WHERE id = :id
                  AND deleted_at IS NULL
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }


    public boolean existsThemeByNameAndDeletedAtIsNull(String name) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM theme
                WHERE name = :name
                  AND deleted_at IS NULL
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("name", name);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }


    public List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, Integer limit) {
        String sql = """
            SELECT t.id, t.name, t.description, t.image_url
            FROM theme t
            JOIN reservation r ON t.id = r.theme_id
            JOIN reservation_time rt ON r.time_id = rt.id
            WHERE r.date BETWEEN :startDate AND :endDate
              AND t.deleted_at IS NULL
              AND r.status = 'ACTIVE'
              AND r.deleted_at IS NULL
              AND rt.deleted_at IS NULL
            GROUP BY t.id, t.name, t.description, t.image_url
            ORDER BY COUNT(r.id) DESC, t.id ASC
            LIMIT :limit
            """;

        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
            "startDate", startDate,
            "endDate", endDate,
            "limit", limit
        ));
        return jdbcTemplate.query(
            sql,
            parameters,
            (resultSet, rowNum) -> Theme.reconstruct(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("image_url"),
                null
            ));
    }
}
