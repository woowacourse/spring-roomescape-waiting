package roomescape.theme.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeRepository {
    private final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) ->
            Theme.of(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Theme save(Theme theme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        insert(theme, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return theme.withId(id);
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query("""
                SELECT id, name, description, thumbnail
                FROM theme
                """, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jdbcTemplate.query("""
                        SELECT id, name, description, thumbnail
                        FROM theme
                        WHERE id = ?
                        """, themeRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Theme> findTopThemesByReservationCount(LocalDate startDate, LocalDate endDate, int limit) {
        return jdbcTemplate.query("""
                        SELECT
                            t.id,
                            t.name,
                            t.description,
                            t.thumbnail
                        FROM theme t
                        INNER JOIN reservation r
                            ON r.theme_id = t.id
                            AND r.status != 'CANCELED'
                        WHERE r.date BETWEEN ? AND ?
                        GROUP BY t.id, t.name, t.description, t.thumbnail
                        ORDER BY COUNT(r.id) DESC
                        LIMIT ?
                        """,
                themeRowMapper, startDate, endDate, limit);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM theme
                WHERE id = ?
                """, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        int rowCount = jdbcTemplate.update("""
                DELETE FROM theme
                WHERE id = ?
                """, id);
        return rowCount > 0;
    }

    private void insert(Theme theme, KeyHolder keyHolder) {
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            INSERT INTO theme (name, description, thumbnail)
                            VALUES (?, ?, ?)
                            """,
                    new String[]{"id"}
            );
            preparedStatement.setString(1, theme.getName());
            preparedStatement.setString(2, theme.getDescription());
            preparedStatement.setString(3, theme.getThumbnail());
            return preparedStatement;
        }, keyHolder);
    }
}
