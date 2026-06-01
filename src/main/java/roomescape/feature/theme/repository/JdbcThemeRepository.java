package roomescape.feature.theme.repository;

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
import org.springframework.stereotype.Repository;
import roomescape.feature.theme.domain.Theme;
import roomescape.global.domain.EntityStatus;
import roomescape.feature.theme.error.type.ThemeErrorType;
import roomescape.global.error.exception.GeneralException;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("theme")
            .usingColumns("name", "description", "image_url", "status")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findAllByNotDeleted() {
        String sql = "SELECT id, name, description, image_url, status FROM theme WHERE status = 'ACTIVE'";
        return jdbcTemplate.query(
            sql,
            (resultSet, rowNum) -> Theme.reconstruct(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("image_url"),
                EntityStatus.valueOf(resultSet.getString("status"))
            ));
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> args = Map.of(
            "name", theme.getName(),
            "description", theme.getDescription(),
            "image_url", theme.getImageUrl(),
            "status", theme.getStatus().name()
        );
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(args).longValue();
        return Theme.reconstruct(generatedKey, theme.getName(), theme.getDescription(), theme.getImageUrl(),
            theme.getStatus());
    }

    @Override
    public void deleteThemeById(Long id) {
        final String sql = "UPDATE theme SET status = 'DELETED' WHERE id = :id AND status = 'ACTIVE'";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);

        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            throw new GeneralException(ThemeErrorType.THEME_NOT_FOUND);
        }
    }

    @Override
    public Optional<Theme> findThemeByIdAndNotDeleted(Long id) {
        final String sql = "SELECT id, name, description, image_url, status FROM theme WHERE id = :id AND status = 'ACTIVE'";
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
                    EntityStatus.valueOf(resultSet.getString("status"))
                )
            );
            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsThemeByIdAndNotDeleted(Long id) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM theme
                WHERE id = :id
                  AND status = 'ACTIVE'
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsThemeByNameAndNotDeleted(String name) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM theme
                WHERE name = :name
                  AND status = 'ACTIVE'
            )
            """;

        SqlParameterSource parameters = new MapSqlParameterSource("name", name);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, Integer limit) {
        String sql = """
            SELECT t.id, t.name, t.description, t.image_url, t.status
            FROM theme t
            JOIN reservation r ON t.id = r.theme_id
            JOIN reservation_time rt ON r.time_id = rt.id
            WHERE r.date BETWEEN :startDate AND :endDate
              AND t.status = 'ACTIVE'
              AND r.status = 'ACTIVE'
              AND rt.status = 'ACTIVE'
            GROUP BY t.id, t.name, t.description, t.image_url, t.status
            ORDER BY COUNT(r.id) DESC, t.id
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
                EntityStatus.valueOf(resultSet.getString("status"))
            ));
    }
}
