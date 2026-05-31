package roomescape.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.projection.ReservationWaitingWithOrder;

@Repository
public class ReservationWaitingQueryJdbcRepository implements ReservationWaitingQueryRepository {

    private static final String SELECT_BASE = """
            SELECT rw.id as waiting_id, rw.name as waiting_name, rw.created_at,
                   (
                       SELECT COUNT(*)
                       FROM reservation_waiting as previous_rw
                       WHERE previous_rw.reservation_id = rw.reservation_id
                       AND (
                           previous_rw.created_at < rw.created_at
                           OR (previous_rw.created_at = rw.created_at AND previous_rw.id <= rw.id)
                       )
                   ) as waiting_order,
                   r.id as reservation_id, r.name as reservation_name, r.date,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description,
                   th.thumbnail_image_url as theme_thumbnail
            FROM reservation_waiting as rw
            INNER JOIN reservation as r ON rw.reservation_id = r.id
            INNER JOIN reservation_time as t ON r.time_id = t.id
            INNER JOIN theme as th ON r.theme_id = th.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingQueryJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ReservationWaitingWithOrder> waitingWithOrderRowMapper = (rs, rowNum) ->
            new ReservationWaitingWithOrder(
                    rs.getLong("waiting_id"),
                    rs.getString("waiting_name"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    mapReservation(rs),
                    rs.getInt("waiting_order")
            );

    @Override
    public Optional<ReservationWaitingWithOrder> findById(Long id) {
        String sql = SELECT_BASE + " WHERE rw.id = ?";
        List<ReservationWaitingWithOrder> results = jdbcTemplate.query(sql, waitingWithOrderRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public List<ReservationWaitingWithOrder> findByName(String name) {
        String sql = SELECT_BASE + " WHERE rw.name = ? ORDER BY rw.created_at ASC, rw.id ASC";
        return jdbcTemplate.query(sql, waitingWithOrderRowMapper, name);
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_value").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );
        return new Reservation(
                rs.getLong("reservation_id"),
                rs.getString("reservation_name"),
                rs.getDate("date").toLocalDate(),
                time,
                theme
        );
    }
}
