package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ThemeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Theme save(final Theme theme) {
        final String sql = """
                INSERT INTO theme (name, description, thumbnail_url)
                VALUES (:name, :description, :thumbnailUrl)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnailUrl", theme.getThumbnailUrl());

        jdbcTemplate.update(sql, param, keyHolder);

        final Long themeId = keyHolder.getKey().longValue();
        return theme.withId(themeId);
    }

    public List<Theme> findAll() {
        final String sql = """
                SELECT id, name, description, thumbnail_url
                FROM theme
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource(), rowMapper()).stream().toList();
    }

    public Optional<Theme> findById(final Long themeId) {
        final String sql = """
                SELECT id, name, description, thumbnail_url
                FROM theme
                WHERE id = :id
                """;

        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("id", themeId);
            final Theme theme = jdbcTemplate.queryForObject(sql, param, rowMapper());
            return Optional.of(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Theme> findPopularThemes(final LocalDate startDate, final LocalDate today) {
        final String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_url
                FROM theme t
                LEFT JOIN reservation r
                    ON r.theme_id = t.id
                    AND r.date >= :startDate
                    AND r.date < :endDate
                GROUP BY
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_url
                ORDER BY
                    COUNT(r.id) DESC,
                    t.name ASC
                LIMIT 10;
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", today);

        return jdbcTemplate.query(sql, param, rowMapper())
                .stream()
                .toList();
    }

    public boolean deleteById(final Long themeId) {
        final String sql = """
                DELETE FROM theme
                WHERE id = :id
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", themeId);

        return jdbcTemplate.update(sql, param) > 0;
    }

    private RowMapper<Theme> rowMapper() {
        return ((rs, rowNum) ->
                Theme.createWithId(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("thumbnail_url")));
    }
}
