package roomescape.reservation.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.Theme;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryThemeRepository implements ThemeRepository {

    private final NamedParameterJdbcTemplate template;

    public InMemoryThemeRepository(final NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public Optional<Theme> findById(final Long themeId) {
        final String sql = "SELECT * FROM theme WHERE id = :id";

        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("id", themeId);
            final Theme theme = template.queryForObject(sql, param, itemRowMapper());

            return Optional.of(theme);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private RowMapper<Theme> itemRowMapper() {
        return ((rs, rowNum) -> Theme.of(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("thumbnail")
        ));
    }

    @Override
    public List<Theme> findAll() {
        final String sql = "SELECT * FROM theme";

        return template.query(sql, itemRowMapper());
    }

    @Override
    public Theme save(final Theme theme) {
        final String sql = "INSERT INTO theme(name, description, thumbnail) VALUES(:name, :description, :thumbnail)";

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", theme.getName().value())
                .addValue("description", theme.getDescription().value())
                .addValue("thumbnail", theme.getThumbnail().value());
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder);

        final long savedThemeId = keyHolder.getKey().longValue();

        return theme.initializeIndex(savedThemeId);
    }

    @Override
    public int deleteById(final Long themeId) {
        final String sql = "DELETE FROM theme WHERE id = :id";
        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", themeId);

        return template.update(sql, param);
    }

    @Override
    public List<Theme> findPopularThemes(final ReservationDate startAt, final ReservationDate endAt, final int maximumThemeCount) {
        final String sql = """
                    SELECT
                        th.id, th.name, th.description, th.thumbnail
                    FROM theme AS th
                    INNER JOIN reservation AS r
                    ON r.theme_id = th.id
                    WHERE r.date BETWEEN :startAt AND :endAt
                    GROUP BY r.theme_id
                    ORDER BY COUNT(r.theme_id) DESC
                    LIMIT :maximumThemeCount
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("startAt", startAt.value())
                .addValue("endAt", endAt.value())
                .addValue("maximumThemeCount", maximumThemeCount);

        return template.query(sql, param, itemRowMapper());
    }
}
