package roomescape.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail_url, price FROM theme";
        return jdbcTemplate.query(sql, rowMapper());
    }

    @Override
    public Optional<Theme> findById(long id) {
        String sql = "SELECT id, name, description, thumbnail_url, price FROM theme WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper(), id).stream().findFirst();
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> params = Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail_url", theme.getThumbnailUrl(),
                "price", theme.getPrice());

        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnailUrl(), theme.getPrice());
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM theme where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Theme> findPopularThemes(Long topCount, LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_url,
                    t.price,
                    count(*) as reservation_count
                FROM (
                    SELECT ts.theme_id
                    FROM reservation r
                        INNER JOIN theme_slot ts ON r.theme_slot_id = ts.id
                    WHERE ts.date BETWEEN ? AND ?
                ) as r

                INNER JOIN theme t
                ON r.theme_id = t.id
                GROUP BY t.id
                ORDER BY reservation_count DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, rowMapper(), fromDate, toDate, topCount);
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
