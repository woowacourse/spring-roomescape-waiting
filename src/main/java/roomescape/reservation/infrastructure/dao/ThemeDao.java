package roomescape.reservation.infrastructure.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.application.repository.ThemeRepository;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.presentation.dto.ThemeRequest;

@Repository
public class ThemeDao implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme insert(ThemeRequest themeRequest) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", themeRequest.getName())
                .addValue("description", themeRequest.getDescription())
                .addValue("thumbnail", themeRequest.getThumbnail());

        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return new Theme(id, themeRequest.getName(), themeRequest.getDescription(), themeRequest.getThumbnail());
    }

    @Override
    public List<Theme> findAllThemes() {
        String sql = """
                SELECT id, name, description, thumbnail
                FROM theme
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new Theme(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        ));
    }

    @Override
    public int delete(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Theme> findById(Long themeId) {
        String sql = """
                SELECT id, name, description, thumbnail
                FROM theme
                WHERE id = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql,
                    (resultSet, rowNum) -> new Theme(
                            resultSet.getLong("id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getString("thumbnail")
                    )
                    , themeId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findPopularThemes() {
        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail
                FROM theme t
                INNER JOIN reservation r ON t.id = r.theme_id
                WHERE r.date BETWEEN DATEADD('DAY', -7, CURRENT_DATE()) AND DATEADD('DAY', -1, CURRENT_DATE())
                GROUP BY t.id, t.name, t.description, t.thumbnail
                ORDER BY COUNT(*) DESC
                LIMIT 10
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new Theme(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        ));
    }
}
