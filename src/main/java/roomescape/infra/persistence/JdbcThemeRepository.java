package roomescape.infra.persistence;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.theme.ThemeRepository;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static Map<String, Object> createParams(Theme theme) {
        return Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail_url", theme.getThumbnailUrl(),
                "price", theme.getPrice()
        );
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail_url, price FROM theme";
        return jdbcTemplate.query(sql, rowMapper());
    }

    @Override
    public Optional<Theme> findById(long id) {
        String sql = "SELECT id, name, description, thumbnail_url, price FROM theme WHERE id = ?";
        List<Theme> themes = jdbcTemplate.query(sql, rowMapper(), id);
        return Optional.ofNullable(DataAccessUtils.singleResult(themes));
    }

    @Override
    public Theme save(Theme theme) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(theme);
        long id = insert.executeAndReturnKey(params).longValue();
        return new Theme(
                id,
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                theme.getPrice()
        );
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM theme where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Theme> findPopularThemes(long limit, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_url,
                    t.price,
                    count(*) as reservation_count
                FROM reservation r
                INNER JOIN reservation_slot rs
                ON r.slot_id = rs.id
                INNER JOIN theme t
                ON rs.theme_id = t.id
                WHERE rs.date BETWEEN ? AND ?
                AND r.status = ?
                GROUP BY t.id
                ORDER BY reservation_count DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                rowMapper(),
                startDate,
                endDate,
                ReservationStatus.RESERVED.name(),
                limit
        );
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingColumns("name", "description", "thumbnail_url", "price")
                .usingGeneratedKeyColumns("id");
    }

    private RowMapper<Theme> rowMapper() {
        return (rs, rowNum) -> new Theme(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("thumbnail_url"),
                rs.getLong("price")
        );
    }
}
