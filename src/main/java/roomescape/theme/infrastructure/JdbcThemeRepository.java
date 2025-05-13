package roomescape.theme.infrastructure;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationPeriod;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static RowMapper<Theme> ROW_MAPPER = (resultSet, rowNum) -> Theme.createWithId(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long save(final Theme theme) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", theme.getName());
        params.put("description", theme.getDescription());
        params.put("thumbnail", theme.getThumbnail());
        return simpleJdbcInsert.executeAndReturnKey(params).longValue();
    }

    @Override
    public List<Theme> findPopularThemes(ReservationPeriod period, int popularCount) {
        String sql = """
                SELECT
                    count(*) as count,
                    t.id as id,
                    t.name as name,
                    t.description as description,
                    t.thumbnail as thumbnail
                FROM theme as t
                LEFT JOIN reservation as r ON t.id = r.theme_id
                WHERE r.date >= ? AND r.date <= ?
                GROUP BY id, name, description, thumbnail
                ORDER BY count DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER, Date.valueOf(period.findStartDate()),
                Date.valueOf(period.findEndDate()), popularCount);
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public boolean deleteById(final Long id) {
        String sql = "DELETE FROM theme where id =?";
        int count = jdbcTemplate.update(sql, id);

        return count != 0;
    }

    @Override
    public Theme findById(final Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }
}
