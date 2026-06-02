package roomescape.theme.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeRepository {
    private final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) ->
            Theme.of(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail"),
                    toLocalDateTime(resultSet.getTimestamp("deleted_at"))
            );

    private final NamedParameterJdbcTemplate jdbcTemplate;

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
                SELECT id, name, description, thumbnail, deleted_at
                FROM theme
                WHERE deleted_at IS NULL
                """, new MapSqlParameterSource(), themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jdbcTemplate.query("""
                        SELECT id, name, description, thumbnail, deleted_at
                        FROM theme
                        WHERE id = :id AND deleted_at IS NULL
                        """, new MapSqlParameterSource("id", id), themeRowMapper)
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
                            t.thumbnail,
                            t.deleted_at
                        FROM theme t
                        INNER JOIN reservation_slot s
                            ON s.theme_id = t.id
                        INNER JOIN reservation r
                            ON r.slot_id = s.id
                            AND r.status != 'CANCELED'
                        WHERE s.date BETWEEN :startDate AND :endDate
                            AND t.deleted_at IS NULL
                        GROUP BY t.id, t.name, t.description, t.thumbnail, t.deleted_at
                        ORDER BY COUNT(r.id) DESC
                        LIMIT :limit
                        """,
                new MapSqlParameterSource()
                        .addValue("startDate", Date.valueOf(startDate))
                        .addValue("endDate", Date.valueOf(endDate))
                        .addValue("limit", limit),
                themeRowMapper);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM theme
                WHERE id = :id AND deleted_at IS NULL
                """, new MapSqlParameterSource("id", id), Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean cancelById(Long id, LocalDateTime now) {
        int rowCount = jdbcTemplate.update("""
                UPDATE theme
                SET deleted_at = :deletedAt
                WHERE id = :id AND deleted_at IS NULL
                """, new MapSqlParameterSource()
                .addValue("deletedAt", Timestamp.valueOf(now))
                .addValue("id", id));
        return rowCount > 0;
    }

    private void insert(Theme theme, KeyHolder keyHolder) {
        jdbcTemplate.update("""
                        INSERT INTO theme (name, description, thumbnail)
                        VALUES (:name, :description, :thumbnail)
                        """,
                new MapSqlParameterSource()
                        .addValue("name", theme.getName())
                        .addValue("description", theme.getDescription())
                        .addValue("thumbnail", theme.getThumbnail()),
                keyHolder,
                new String[]{"id"});
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
