package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public Theme save(final Theme themeWithoutId) {
        final long themeId = insertTheme(themeWithoutId);

        return themeWithoutId.withId(themeId);
    }

    public boolean deleteById(final Long themeId) {
        final String sql = """
                DELETE FROM theme
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, themeId) > 0;
    }

    public List<Theme> findAll() {
        final String sql = """
                SELECT id, name, description, thumbnail_url, price
                FROM theme
                """;

        return jdbcTemplate.query(sql, this::mapToDomain).stream().toList();
    }

    public Optional<Theme> findById(final Long themeId) {
        final String sql = """
                SELECT id, name, description, thumbnail_url, price
                FROM theme
                WHERE id = ?
                """;

        try {
            final Theme theme = jdbcTemplate.queryForObject(
                    sql,
                    this::mapToDomain,
                    themeId
            );

            return Optional.of(theme);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Theme> findPopularThemes(final LocalDate startDate, final LocalDate today) {
        final String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_url,
                    t.price
                FROM theme t
                LEFT JOIN reservation r
                    ON r.theme_id = t.id
                    AND r.date >= ?
                    AND r.date < ?
                GROUP BY
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_url,
                    t.price
                ORDER BY
                    COUNT(r.id) DESC,
                    t.name ASC
                LIMIT 10;
                """;

        return jdbcTemplate.query(
                        sql,
                        this::mapToDomain,
                        startDate,
                        today
                )
                .stream()
                .toList();
    }

    private long insertTheme(final Theme theme) {
        final String sql = """
                INSERT INTO theme (name, description, thumbnail_url, price)
                VALUES (?, ?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setString(1, theme.getName());
            preparedStatement.setString(2, theme.getDescription());
            preparedStatement.setString(3, theme.getThumbnailUrl());
            preparedStatement.setLong(4, theme.getPrice());

            return preparedStatement;
        }, keyHolder);

        return JdbcUtil.extractGeneratedKey(keyHolder);
    }

    /**
     * ResultSet - Domain 매핑 메서드
     */
    private Theme mapToDomain(final ResultSet resultSet, final int rowNum) throws SQLException {
        return Theme.createWithId(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url"),
                resultSet.getLong("price")
        );
    }
}
