package roomescape.repository.history;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
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
            getNullableLocalDateTime(resultSet, "requested_at")
    );

    private static final RowMapper<MyWaitingOrder> waitingOrderRowMapper = (resultSet, rowNumber) -> new MyWaitingOrder(
            resultSet.getLong("reservation_id"),
            resultSet.getLong("waiting_id"),
            resultSet.getTimestamp("requested_at").toLocalDateTime()
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
                       CAST(NULL AS TIMESTAMP) AS requested_at
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
                       rw.requested_at
                FROM reservation_waiting AS rw
                INNER JOIN reservation AS r ON rw.reservation_id = r.id
                INNER JOIN theme AS t ON r.theme_id = t.id
                INNER JOIN reservation_time AS rt ON r.time_id = rt.id
                WHERE rw.name = ?
                ORDER BY date, start_at, status
                """;

        return jdbcTemplate.query(sql, historyRowMapper, name, name);
    }

    @Override
    public List<MyWaitingOrder> findWaitingOrdersByReservationIds(final List<Long> reservationIds) {
        if (reservationIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", Collections.nCopies(reservationIds.size(), "?"));
        String sql = """
                SELECT reservation_id,
                       id AS waiting_id,
                       requested_at
                FROM reservation_waiting
                WHERE reservation_id IN (%s)
                """.formatted(placeholders);

        return jdbcTemplate.query(sql, waitingOrderRowMapper, reservationIds.toArray());
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

    private static LocalDateTime getNullableLocalDateTime(
            final ResultSet resultSet,
            final String columnName
    ) throws SQLException {
        Timestamp value = resultSet.getTimestamp(columnName);

        if (value == null) {
            return null;
        }

        return value.toLocalDateTime();
    }
}
