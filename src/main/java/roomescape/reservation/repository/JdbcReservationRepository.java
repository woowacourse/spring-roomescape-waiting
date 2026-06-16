package roomescape.reservation.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> Reservation.load(
            rs.getLong("reservation_id"),
            rs.getString("name"),
            rs.getLong("slot_id"),
            ReservationStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("reserved_at").toLocalDateTime()
    );

    public JdbcReservationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.slot_id, r.status, r.reserved_at
                FROM reservation r
                WHERE r.id = :id
                """;
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, reservationRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ReservationWithSlotInformation> findAll() {
        String sql = """
                SELECT
                    r.id            AS reservation_id,
                    r.name          AS name,
                    r.status        AS status,
                    r.reserved_at   AS reserved_at,
                    s.id            AS slot_id,
                    rd.date         AS date,
                    rt.start_at     AS start_at,
                    t.id            AS theme_id,
                    t.name          AS theme_name,
                    t.thumbnail_url AS thumbnail_url
                FROM reservation r
                    JOIN reservation_slot s  ON r.slot_id  = s.id
                    JOIN reservation_date rd ON s.date_id  = rd.id
                    JOIN reservation_time rt ON s.time_id  = rt.id
                    JOIN theme            t  ON s.theme_id = t.id
                ORDER BY rd.date ASC, rt.start_at ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ReservationWithSlotInformation(
                rs.getLong("reservation_id"),
                rs.getLong("slot_id"),
                rs.getString("name"),
                rs.getObject("date", LocalDate.class),
                rs.getObject("start_at", LocalTime.class),
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("thumbnail_url"),
                ReservationStatus.valueOf(rs.getString("status")),
                rs.getObject("reserved_at", LocalDateTime.class),
                null
        ));
    }

    @Override
    public List<ReservationWithSlotInformation> findByMemberName(String name) {
        String sql = """
                SELECT
                    r.id            AS reservation_id,
                    r.name          AS name,
                    r.status        AS status,
                    r.reserved_at   AS reserved_at,
                    s.id            AS slot_id,
                    rd.date         AS date,
                    rt.start_at     AS start_at,
                    t.id            AS theme_id,
                    t.name          AS theme_name,
                    t.thumbnail_url AS thumbnail_url,
                    CASE
                        WHEN r.status = 'WAITING' THEN (
                            SELECT COUNT(*) + 1
                            FROM reservation wait
                            WHERE wait.slot_id = r.slot_id
                              AND wait.status = 'WAITING'
                              AND (wait.reserved_at < r.reserved_at
                                   OR (wait.reserved_at = r.reserved_at AND wait.id < r.id))
                        )
                        ELSE NULL
                    END AS waiting_turn
                FROM reservation r
                    JOIN reservation_slot s  ON r.slot_id  = s.id
                    JOIN reservation_date rd ON s.date_id  = rd.id
                    JOIN reservation_time rt ON s.time_id  = rt.id
                    JOIN theme            t  ON s.theme_id = t.id
                WHERE r.name = :name
                ORDER BY rd.date ASC, rt.start_at ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("name", name);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new ReservationWithSlotInformation(
                rs.getLong("reservation_id"),
                rs.getLong("slot_id"),
                rs.getString("name"),
                rs.getObject("date", LocalDate.class),
                rs.getObject("start_at", LocalTime.class),
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("thumbnail_url"),
                ReservationStatus.valueOf(rs.getString("status")),
                rs.getObject("reserved_at", LocalDateTime.class),
                rs.getObject("waiting_turn", Long.class)
        ));
    }

    @Override
    public List<Reservation> findReservedAndWaitingBySlotId(Long slotId) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.slot_id, r.status, r.reserved_at
                FROM reservation r
                WHERE r.slot_id = :slotId
                  AND r.status IN ('RESERVED', 'WAITING', 'PENDING_PAYMENT')
                ORDER BY r.reserved_at ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("slotId", slotId);
        return jdbcTemplate.query(sql, params, reservationRowMapper);
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("slot_id", reservation.getSlotId())
                .addValue("status", reservation.getStatus().name())
                .addValue("reserved_at", reservation.getReservedAt());
        Long savedId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Reservation.load(savedId, reservation.getName(), reservation.getSlotId(),
                reservation.getStatus(), reservation.getReservedAt());
    }

    @Override
    public boolean updateStatus(Reservation reservation) {
        String sql = "UPDATE reservation SET status = :status WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reservation.getId())
                .addValue("status", reservation.getStatus().name());
        return jdbcTemplate.update(sql, params) > 0;
    }

    @Override
    public boolean updateSchedule(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET 
                    slot_id = :slotId,
                    status = :status,
                    reserved_at = :reserved_at
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", reservation.getSlotId())
                .addValue("status", reservation.getStatus().name())
                .addValue("id", reservation.getId())
                .addValue("reserved_at", reservation.getReservedAt());
        return jdbcTemplate.update(sql, params) > 0;
    }

}
