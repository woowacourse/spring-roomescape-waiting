package roomescape.repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationWaiting;

@Repository
public class ReservationWaitingJdbcRepository implements ReservationWaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        int waitingOrder = calculateWaitingOrder(reservationWaiting);
        String sql = "INSERT INTO reservation_waiting (name, created_at, reservation_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservationWaiting.getName());
            ps.setTimestamp(2, Timestamp.valueOf(reservationWaiting.getCreatedAt()));
            ps.setLong(3, reservationWaiting.getReservation().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return new ReservationWaiting(
                id,
                reservationWaiting.getName(),
                reservationWaiting.getCreatedAt(),
                reservationWaiting.getReservation(),
                waitingOrder
        );
    }

    private int calculateWaitingOrder(ReservationWaiting reservationWaiting) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation_waiting
                WHERE reservation_id = ?
                AND created_at <= ?
                """;
        Integer waitingCount = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                reservationWaiting.getReservation().getId(),
                Timestamp.valueOf(reservationWaiting.getCreatedAt())
        );

        return waitingCount != null ? waitingCount + 1 : 1;
    }

}
