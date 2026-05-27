package roomescape.repository;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Reservation;
import roomescape.domain.Status;

@Repository
public class ReservationDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getLong("reservation_slot_id"),
            Status.valueOf(rs.getString("status")),
            rs.getObject("updated_at", LocalDateTime.class)
    );

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(String name, Long reservationSlotId, LocalDateTime now) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "name", name,
                "reservation_slot_id", reservationSlotId,
                "status", Status.RESERVED.name(),
                "updated_at", now
        )).longValue();
    }

    public Reservation findById(long id) {
        String sql = """
                SELECT  r.id,
                        r.name AS name,
                        r.reservation_slot_id AS reservation_slot_id,
                        r.status AS status,
                        r.updated_at AS updated_at
                FROM reservation AS r
                where r.id = ?
                """;
        return jdbcTemplate.queryForObject(sql, reservationRowMapper, id);
    }

    public boolean existByReservationId(long reservationId) {
        String sql = """
            SELECT EXISTS(
                SELECT 1
                FROM reservation r
                WHERE r.reservation_slot_id = ?
            )
            """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, reservationId
                )
        );
    }

    public int findOrderByReservationId(long reservationId, long slotId) {
        String sql = """
            SELECT COUNT(*)
            FROM reservation r
            WHERE r.reservation_slot_id = ?
              AND r.updated_at < (
                  SELECT updated_at
                  FROM reservation r2
                  WHERE r2.id = ?
              )
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                slotId,
                reservationId
        );
    }

    public boolean existsByReservation(Long reservationId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM reservation r
            WHERE r.reservation_slot_id = ?
            """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, reservationId));
    }
    public void update(Long reservationId, Long reservationSlotId) {
        jdbcTemplate.update("UPDATE reservation SET reservation_slot_id = ?  WHERE id = ?", reservationSlotId , reservationId);
    }

    public void delete(Long id, Status status) {
        jdbcTemplate.update("UPDATE reservation SET status = ?  WHERE id = ?", status.name() , id);
    }
}
