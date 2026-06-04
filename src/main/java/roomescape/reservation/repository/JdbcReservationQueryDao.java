package roomescape.reservation.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.query.dto.PopularThemeQueryResult;
import roomescape.reservation.query.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcReservationQueryDao implements ReservationQueryDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationQueryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationWithStatusResult> queryAllByNameWithStatus(String name) {
        String sql = """
                SELECT id, name, reservation_date, time_id, time_start_at,
                       theme_id, theme_name, theme_description, theme_thumbnail_url,
                       status, waiting_order
                FROM (
                    SELECT r.id AS id,
                           r.name AS name,
                           r.reservation_date,
                           r.time_id,
                           t.start_at AS time_start_at,
                           h.id AS theme_id,
                           h.name AS theme_name,
                           h.description AS theme_description,
                           h.thumbnail_url AS theme_thumbnail_url,
                           'reserved' AS status,
                           0 AS waiting_order
                    FROM reservation r
                    INNER JOIN reservation_time t ON r.time_id = t.id
                    INNER JOIN theme h ON r.theme_id = h.id
                    WHERE r.name = ?
                    UNION ALL
                    SELECT ranked.id,
                           ranked.name,
                           ranked.reservation_date,
                           ranked.time_id,
                           ranked.time_start_at,
                           ranked.theme_id,
                           ranked.theme_name,
                           ranked.theme_description,
                           ranked.theme_thumbnail_url,
                           ranked.status,
                           ranked.waiting_order
                    FROM (
                        SELECT rw.id AS id,
                               rw.name AS name,
                               rw.reservation_date AS reservation_date,
                               rw.time_id,
                               t.start_at AS time_start_at,
                               h.id AS theme_id,
                               h.name AS theme_name,
                               h.description AS theme_description,
                               h.thumbnail_url AS theme_thumbnail_url,
                               'waiting' AS status,
                               ROW_NUMBER() OVER (PARTITION BY rw.reservation_date, rw.time_id, rw.theme_id ORDER BY rw.id) AS waiting_order
                        FROM reservation_waiting rw
                        INNER JOIN reservation_time t ON rw.time_id = t.id
                        INNER JOIN theme h ON rw.theme_id = h.id
                    ) ranked
                    WHERE ranked.name = ?
                ) combined
                ORDER BY reservation_date, time_id, theme_id, status
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReservationTime time = new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getTime("time_start_at").toLocalTime()
            );
            Theme theme = new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail_url")
            );
            return new ReservationWithStatusResult(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getDate("reservation_date").toLocalDate(),
                    time,
                    theme,
                    rs.getString("status"),
                    rs.getLong("waiting_order")
            );
        }, name, name);
    }

    @Override
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
