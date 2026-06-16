package roomescape.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;

@Repository
public class ThemeDao {

    private static final RowMapper<Theme> themeRowMapper = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail_url"),
            rs.getInt("price")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(String name, String description, String thumbnailUrl, int price) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "name", name,
                "description", description,
                "thumbnail_url", thumbnailUrl,
                "price", price
        )).longValue();
    }

    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }

    public List<Theme> findAll() {
        return jdbcTemplate.query("SELECT id, name, description, thumbnail_url, price FROM theme", themeRowMapper);
    }

    public List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, ReservationStatus status, int limit) {
        String sql = """
            SELECT
                th.id,
                th.name,
                th.description,
                th.thumbnail_url,
                th.price
            FROM reservation r
            INNER JOIN schedule s ON r.schedule_id = s.id
            INNER JOIN theme th ON s.theme_id = th.id
            WHERE s.date >= ?
                 AND s.date < ?
                 AND r.status = ?
             GROUP BY
                th.id,
                th.name,
                th.description,
                th.thumbnail_url,
                th.price
            ORDER BY COUNT(r.id) DESC, th.id ASC
            LIMIT ?
            """;

        return jdbcTemplate.query(
                sql,
                themeRowMapper,
                Date.valueOf(startDate),
                Date.valueOf(endDate),
                status.name(),
                limit
        );
    }

    public boolean existsByName(String name) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM theme
                WHERE name = ?
            )
            """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, name)
        );
    }
}
