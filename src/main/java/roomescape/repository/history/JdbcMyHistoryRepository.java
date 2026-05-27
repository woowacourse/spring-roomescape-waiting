package roomescape.repository.history;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
public class JdbcMyHistoryRepository implements MyHistoryRepository {

    private static final RowMapper<MyHistory> historyRowMapper = (resultSet, rowNumber) -> new MyHistory(
            resultSet.getLong("reservation_id"),
            getNullableLong(resultSet, "waiting_id"),
            resultSet.getString("status"),
            resultSet.getString("history_name"),
            resultSet.getDate("date").toLocalDate(),
            mapTheme(resultSet),
            mapReservationTime(resultSet),
            resultSet.getInt("sequence")
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcMyHistoryRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MyHistory> findByUserName(final String name) {
        String sql = """
                SELECT 'RESERVATION' AS status,
                       r.id AS reservation_id,
                       NULL AS waiting_id,
                       r.name AS history_name,
                       r.date,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url,
                       rt.id AS time_id,
                       rt.start_at,
                       0 AS sequence
                FROM reservation AS r
                INNER JOIN theme AS t ON r.theme_id = t.id
                INNER JOIN reservation_time AS rt ON r.time_id = rt.id
                WHERE r.name = ?

                UNION ALL

                SELECT 'WAITING' AS status,
                       r.id AS reservation_id,
                       rw.id AS waiting_id,
                       rw.name AS history_name,
                       r.date,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url,
                       rt.id AS time_id,
                       rt.start_at,
                       (
                           SELECT COUNT(*)
                           FROM reservation_waiting AS earlier
                           WHERE earlier.reservation_id = rw.reservation_id
                             AND (
                                 earlier.requested_at < rw.requested_at
                                 OR (earlier.requested_at = rw.requested_at AND earlier.id <= rw.id)
                             )
                       ) AS sequence
                FROM reservation_waiting AS rw
                INNER JOIN reservation AS r ON rw.reservation_id = r.id
                INNER JOIN theme AS t ON r.theme_id = t.id
                INNER JOIN reservation_time AS rt ON r.time_id = rt.id
                WHERE rw.name = ?
                ORDER BY date, start_at, status
                """;

        return jdbcTemplate.query(sql, historyRowMapper, name, name);
    }

    private static Theme mapTheme(final ResultSet resultSet) throws SQLException {
        return Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url")
        );
    }

    private static ReservationTime mapReservationTime(final ResultSet resultSet) throws SQLException {
        return ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );
    }

    private static Long getNullableLong(final ResultSet resultSet, final String columnName) throws SQLException {
        long value = resultSet.getLong(columnName);

        if (resultSet.wasNull()) {
            return null;
        }

        return value;
    }
}
