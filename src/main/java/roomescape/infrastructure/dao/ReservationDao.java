package roomescape.infrastructure.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Reservation;
import roomescape.domain.Status;
import roomescape.dto.ReservationResponse;

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
                    rs.getInt("waiting_order")
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
                       (
                           SELECT COUNT(*)
                           FROM reservation rv2
                           WHERE rv2.reservation_slot_id = rv.reservation_slot_id
                             AND rv2.status = 'RESERVED'
                             AND rv2.id < rv.id
                       ) AS waiting_order
                FROM reservation AS rv
                INNER JOIN reservation_slot AS rs ON rv.reservation_slot_id = rs.id
                INNER JOIN reservation_time AS t ON rs.time_id = t.id
                INNER JOIN theme AS th ON rs.theme_id = th.id
                WHERE rv.name = ?
                """;
        return jdbcTemplate.query(sql, reservationResponseRowMapper, username);
    }

    public boolean existByNameReservationIdStatus(String name, Long reservationSlotId, Status status) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE name = ?
                      AND reservation_slot_id = ?
                      AND status = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, name, reservationSlotId, status.name());
    }

    public void update(Long reservationId, Long reservationSlotId) {
        jdbcTemplate.update("UPDATE reservation SET reservation_slot_id = ?  WHERE id = ?", reservationSlotId , reservationId);
    }

    public void delete(Long id, Status status) {
        jdbcTemplate.update("UPDATE reservation SET status = ?  WHERE id = ?", status.name() , id);
    }
}
