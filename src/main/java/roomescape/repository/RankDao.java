package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

@Repository
public class RankDao {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Theme> rowMapper =
            (resultSet, rowNum) -> new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    public RankDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Theme> findTopTenByDate(final LocalDate fromDate, final LocalDate toDate) {
        String sql = """
                 select theme.id, theme.name, theme.description, theme.thumbnail
                 from theme
                 left join reservation on reservation.theme_id = theme.id
                 and reservation.date between ? and ?
                 group by theme.id
                 order by count(theme_id) desc limit 10;
                """;
        return jdbcTemplate.query(sql, rowMapper, fromDate, toDate);
    }
}
