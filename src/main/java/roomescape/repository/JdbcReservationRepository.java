package roomescape.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
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

    @Override
    public Optional<Reservation> findReservationById(long id) {
        String sql = SELECT_RESERVATION_SQL + " WHERE r.id = ?";
        return jdbcTemplate.query(sql, reservationRowMapper, id).stream()
                .findFirst();
    }

    @Override
    public Optional<Reservation> findReservationBySlotId(Long slotId) {
        String sql = SELECT_RESERVATION_SQL + " WHERE r.slot_id = ?";
        return jdbcTemplate.query(sql, reservationRowMapper, slotId).stream()
                .findFirst();
    }

    @Override
    public List<Reservation> findAllReservations() {
        return jdbcTemplate.query(SELECT_RESERVATION_SQL, reservationRowMapper);
    }

    @Override
    public List<Reservation> findAllByName(String name) {
        String sql = SELECT_RESERVATION_SQL + " WHERE r.name = ?";
        return jdbcTemplate.query(sql, reservationRowMapper, name);
    }

    @Override
    public boolean isExistBySlot(long slotId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM reservation WHERE slot_id = ?
                )
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, slotId);
    }

    @Override
    public Long insert(Reservation reservation) {
        String sql = "insert into reservation(slot_id, name, created_at) values(?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reservation.getSlot().getId());
            ps.setString(2, reservation.getName());
            ps.setObject(3, reservation.getCreatedAt());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateName(Long id, String name) {
        String sql = "update reservation set name = ? where id = ?";
        jdbcTemplate.update(sql, name, id);
    }

    @Override
    public long update(Long id, String name, Long slotId, LocalDateTime createdAt) {
        String sql = "update reservation set slot_id = ?, name = ?, created_at = ? where id = ?";
        return jdbcTemplate.update(sql, slotId, name, createdAt, id);
    }

    @Override
    public long delete(Long id) {
        String sql = "delete from reservation where id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
