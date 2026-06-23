package roomescape.infrastructure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import roomescape.domain.Reservation;
import roomescape.domain.Status;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.dto.ReservationResponse;

@Repository
public class JdbcReservationRepository implements ReservationQueryRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getLong("reservation_slot_id"),
            Status.valueOf(rs.getString("status")),
            rs.getObject("updated_at", LocalDateTime.class)
    );

    private final RowMapper<ReservationResponse> reservationResponseRowMapper = (rs, rowNum) ->
            new ReservationResponse(
                    rs.getLong("reservation_id"),
                    rs.getString("name"),
                    rs.getString("status"),
                    rs.getDate("date").toLocalDate(),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail"),
                    rs.getTime("time_value").toLocalTime(),
                    rs.getInt("waiting_order"),
                    null,
                    null
            );

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationResponse> findByUserName(String username) {
        String sql = """
                SELECT rv.id AS reservation_id,
                       rv.name AS name,
                       rv.status AS status,
                       rs.date AS date,
                       th.name AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail_url AS theme_thumbnail,
                       t.start_at AS time_value,
                       CASE
                           WHEN rv.status = 'RESERVED' THEN 0
                           WHEN rv.status = 'CANCELED' THEN 0
                           ELSE (
                               SELECT COUNT(*)
                               FROM reservation rv2
                               WHERE rv2.reservation_slot_id = rv.reservation_slot_id
                                 AND rv2.status = 'WAITING'
                                 AND (
                                     rv2.updated_at < rv.updated_at
                                     OR (rv2.updated_at = rv.updated_at AND rv2.id < rv.id)
                                 )
                           ) + 1
                       END AS waiting_order
                FROM reservation AS rv
                INNER JOIN reservation_slot AS rs ON rv.reservation_slot_id = rs.id
                INNER JOIN reservation_time AS t ON rs.time_id = t.id
                INNER JOIN theme AS th ON rs.theme_id = th.id
                WHERE rv.name = ?
                """;
        return jdbcTemplate.query(sql, reservationResponseRowMapper, username);
    }
}
