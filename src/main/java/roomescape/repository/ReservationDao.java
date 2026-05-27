package roomescape.repository;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Reservation;

@Repository
public class ReservationDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Reservation> waitingRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getLong("reservation_id")
    );

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(String name, Long reservationId) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "name", name,
                "reservation_id", reservationId
        )).longValue();
    }

    public Reservation findById(long id) {
        String sql = """
                SELECT  w.id AS wait_id,
                        w.name AS name,
                        w.reservation_id AS reservation_id
                FROM reservation AS w
                where w.id = ?
                """;
        return jdbcTemplate.queryForObject(sql, waitingRowMapper, id);
    }

    public boolean existByReservationId(long reservationId) {
        String sql = """
            SELECT EXISTS(
                SELECT 1
                FROM reservation w
                WHERE w.reservation_id = ?
            )
            """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, reservationId
                )
        );
    }

    public int findOrderByReservationId(long id, long reservationId) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation w
                WHERE w.reservation_id = ?
                  AND w.id < ?
                """;
        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                reservationId,
                id
        );
    }

    public boolean existsByReservation(Long reservationId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM reservation w
            WHERE w.reservation_id = ?
            """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, reservationId));
    }

    public Reservation findByReservationIdAndName(Long reservationId, String name) {
        String sql = """
            SELECT w.id AS id,
                   w.name AS name,
                   w.reservation_id AS reservation_id
            FROM reservation w
            WHERE w.reservation_id = ?
              AND w.name = ?
            """;
        return jdbcTemplate.queryForObject(sql, waitingRowMapper, reservationId, name);
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }
}
