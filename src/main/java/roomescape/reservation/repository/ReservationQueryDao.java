package roomescape.reservation.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.query.dto.PopularThemeQueryResult;

@Repository
public class ReservationQueryDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationQueryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PopularThemeQueryResult> queryPopularThemes(LocalDate from, LocalDate to, int limit) {
        String sql = """
                SELECT t.id,
                       t.name,
                       t.description,
                       t.thumbnail_url
                FROM reservation r
                INNER JOIN theme t
                  ON r.theme_id = t.id
                WHERE r.reservation_date >= ?
                  AND r.reservation_date <= ?
                GROUP BY t.id,
                         t.name,
                         t.description,
                         t.thumbnail_url
                ORDER BY COUNT(r.id) DESC,
                         t.id ASC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> new PopularThemeQueryResult(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("thumbnail_url")
                ),
                Date.valueOf(from),
                Date.valueOf(to),
                limit
        );
    }
}
