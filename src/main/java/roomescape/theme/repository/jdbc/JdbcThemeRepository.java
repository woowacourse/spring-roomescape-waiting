package roomescape.theme.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.repository.entity.ThemeEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Theme> findAll() {
        final String sql = """
                SELECT id, name, description, thumbnail_url, price
                FROM theme
                """;

        return jdbcTemplate.query(sql, this::mapToDomain).stream().toList();
    }

    @Override
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
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Theme save(final Theme themeWithoutId) {
        final ThemeEntity themeEntity = toEntity(themeWithoutId);

        final long themeId = insertTheme(themeEntity);

        return Theme.of(
                themeId,
                themeWithoutId.getName(),
                themeWithoutId.getDescription(),
                themeWithoutId.getThumbnailUrl(),
                themeWithoutId.getPrice()
        );
    }

    @Override
    public boolean deleteById(final Long themeId) {
        final String sql = """
                DELETE FROM theme
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, themeId) > 0;
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startDate, LocalDate today) {
        final String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_url,
                    t.price
                FROM theme t
                LEFT JOIN reservation_slot s
                    ON s.theme_id = t.id
                    AND s.reservation_date >= ?
                    AND s.reservation_date < ?
                LEFT JOIN reservation r ON r.slot_id = s.id AND r.status = 'CONFIRMED'
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

    private long insertTheme(final ThemeEntity themeEntity) {
        final String sql = """
                INSERT INTO theme (name, description, thumbnail_url, price)
                VALUES (?, ?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );

            preparedStatement.setString(1, themeEntity.name());
            preparedStatement.setString(2, themeEntity.description());
            preparedStatement.setString(3, themeEntity.thumbnailUrl());
            preparedStatement.setInt(4, themeEntity.price());

            return preparedStatement;
        }, keyHolder);

        return generatedIdFrom(keyHolder);
    }

    private static long generatedIdFrom(final KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성된 id를 가져오지 못했습니다.");
        }

        return keyHolder.getKey().longValue();
    }

    private Theme mapToDomain(final ResultSet resultSet, final int rowNum) throws SQLException {
        return Theme.of(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url"),
                resultSet.getInt("price")
        );
    }

    private ThemeEntity toEntity(final Theme theme) {
        return new ThemeEntity(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                theme.getPrice()
        );
    }
}
