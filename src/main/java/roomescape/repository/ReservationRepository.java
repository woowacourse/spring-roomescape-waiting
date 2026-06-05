package roomescape.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;

import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepository {
    private static final String SELECT_BASE = """
        SELECT r.id          AS reservation_id,
               r.name        AS reservation_name,
               r.status      AS reservation_status,
               r.slot_id     AS slot_id,
               s.date        AS slot_date,
               t.id          AS reservation_time_id,
               t.start_at    AS reservation_time_start_at,
               th.id         AS theme_id,
               th.name       AS theme_name,
               th.description AS theme_description,
               th.thumbnail_url AS theme_thumbnail_url
        FROM reservation r
        JOIN slot s              ON r.slot_id = s.id
        JOIN reservation_time t  ON s.time_id = t.id
        JOIN theme th            ON s.theme_id = th.id
        """;

    private static final RowMapper<Reservation> ROW_MAPPER = (rs, rowNum) -> {
        ReservationTime reservationTime = ReservationTime.of(
                rs.getLong("reservation_time_id"),
                rs.getTime("reservation_time_start_at").toLocalTime()
        );

        Theme theme = Theme.load(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail_url")
        );

        Slot slot = Slot.load(
                rs.getLong("slot_id"),
                rs.getDate("slot_date").toLocalDate(),
                reservationTime,
                theme
        );

        return Reservation.load(
                rs.getLong("reservation_id"),
                rs.getString("reservation_name"),
                rs.getString("reservation_status"),
                slot
        );
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id")
                .usingColumns("slot_id", "name", "status");
    }

    

    public Reservations findAll() {
        return new Reservations(jdbcTemplate.query(SELECT_BASE, ROW_MAPPER));
    }

    

    public Optional<Reservation> findById(Long id) {
        List<Reservation> result = jdbcTemplate.query(
                SELECT_BASE + "WHERE r.id = :id",
                new MapSqlParameterSource("id", id),
                ROW_MAPPER);
        return result.stream().findFirst();
    }


    public Reservations findByName(String name) {
        MapSqlParameterSource param = new MapSqlParameterSource("name", name);
        return new Reservations(jdbcTemplate.query(
                SELECT_BASE + "WHERE r.name = :name",
                param,
                ROW_MAPPER));
    }

    
    public Reservations findBySlotId(Long slotId) {
        MapSqlParameterSource param = new MapSqlParameterSource("slotId", slotId);
        return new Reservations(jdbcTemplate.query(
                SELECT_BASE + "WHERE r.slot_id = :slotId ORDER BY r.created_at ASC",
                param,
                ROW_MAPPER));
    }

    
    public Optional<Reservation> findFirstWaitingBySlotId(Long slotId) {
        MapSqlParameterSource param = new MapSqlParameterSource("slotId", slotId);
        List<Reservation> result = jdbcTemplate.query(
                SELECT_BASE + "WHERE r.slot_id = :slotId AND r.status = 'WAITING' ORDER BY r.created_at ASC LIMIT 1",
                param,
                ROW_MAPPER);
        return result.stream().findFirst();
    }

    
    public boolean existsBySlotIdAndName(Long slotId, String name) {
        MapSqlParameterSource params = new MapSqlParameterSource("slotId", slotId)
                .addValue("name", name);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                        SELECT EXISTS (
                            SELECT 1 FROM reservation WHERE slot_id = :slotId AND name = :name
                        )
                        """,
                params,
                Boolean.class));
    }

    

    public boolean existsApprovedBySlotId(Long slotId) {
        MapSqlParameterSource param = new MapSqlParameterSource("slotId", slotId);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                        SELECT EXISTS (
                            SELECT 1 FROM reservation WHERE slot_id = :slotId AND status = 'APPROVED'
                        )
                        """,
                param,
                Boolean.class));
    }

    public boolean existsApprovedBySlotIdExcluding(Long slotId, Long excludeId) {
        MapSqlParameterSource params = new MapSqlParameterSource("slotId", slotId)
                .addValue("excludeId", excludeId);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                        SELECT EXISTS (
                            SELECT 1 FROM reservation
                            WHERE slot_id = :slotId AND status = 'APPROVED' AND id != :excludeId
                        )
                        """,
                params,
                Boolean.class));
    }

    

    public Reservation update(Long id, Reservation reservation) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id)
                .addValue("slot_id", reservation.getSlotId())
                .addValue("name", reservation.getName().getValue())
                .addValue("status", reservation.getStatus().name());
        jdbcTemplate.update("UPDATE reservation SET slot_id = :slot_id, name = :name, status = :status WHERE id = :id", params);
        return findById(id).orElseThrow();
    }

    

    public void updateStatusById(Long id, Status status) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id)
                .addValue("status", status.name());

        jdbcTemplate.update("UPDATE reservation SET status = :status WHERE id = :id", params);
    }

    

    public Reservation save(Reservation reservation) {
        MapSqlParameterSource params = new MapSqlParameterSource("slot_id", reservation.getSlotId())
                .addValue("name", reservation.getName().getValue())
                .addValue( "status", reservation.getStatus().name());

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return reservation.withId(generatedKey);
    }

    

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = :id",
                new MapSqlParameterSource("id", id));
    }


    public boolean existsById(Long id) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", id);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM reservation WHERE id = :id
                )
                """,
                param,
                Boolean.class));
    }
}
