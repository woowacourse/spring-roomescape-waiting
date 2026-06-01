package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationQueryingDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationQueryingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_RESERVATION_SQL = """
            SELECT r.id AS reservation_id,
                   r.name AS reservation_name,
                   r.created_at AS reservation_created_at,
                   s.id AS slot_id,
                   s.date AS slot_date,
                   t.id AS time_id,
                   t.start_at AS time_start_at,
                   th.id AS theme_id,
                   th.name AS theme_name,
                   th.description AS theme_description,
                   th.url AS theme_url
            FROM reservation r
            JOIN slot s ON r.slot_id = s.id
            JOIN reservation_time t ON s.time_id = t.id
            JOIN theme th ON s.theme_id = th.id
            """;

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("time_start_at", LocalTime.class)
        );
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_url")
        );
        Slot slot = Slot.restore(
                resultSet.getLong("slot_id"),
                resultSet.getObject("slot_date", LocalDate.class),
                time,
                theme
        );
        return Reservation.restore(
                resultSet.getLong("reservation_id"),
                slot,
                resultSet.getString("reservation_name"),
                resultSet.getObject("reservation_created_at", LocalDateTime.class)
        );
    };

    public Optional<Reservation> findReservationById(long id) {
        String sql = SELECT_RESERVATION_SQL + " WHERE r.id = ?";
        return jdbcTemplate.query(sql, reservationRowMapper, id).stream()
                .findFirst();
    }

    public Optional<Reservation> findReservationBySlotId(Long slotId) {
        String sql = SELECT_RESERVATION_SQL + " WHERE r.slot_id = ?";
        return jdbcTemplate.query(sql, reservationRowMapper, slotId).stream()
                .findFirst();
    }

    public List<Reservation> findAllReservations() {
        return jdbcTemplate.query(SELECT_RESERVATION_SQL, reservationRowMapper);
    }

    public List<Reservation> findAllByName(String name) {
        String sql = SELECT_RESERVATION_SQL + " WHERE r.name = ?";
        return jdbcTemplate.query(sql, reservationRowMapper, name);
    }
}
