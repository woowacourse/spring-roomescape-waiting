package roomescape.infra.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.reservation.ReservationRepository;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT 
                    r.id AS r_id, 
                    r.name, 
                    r.created_at,
                    r.status,
                    rs.id AS slot_id,
                    rs.date AS slot_date,
                    ts.id AS time_id,
                    ts.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.price AS theme_price
                FROM reservation r 
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                INNER JOIN time_slot ts ON rs.time_id = ts.id 
                INNER JOIN theme th ON rs.theme_id = th.id
                """;
        return jdbcTemplate.query(sql, rowMapper());
    }

    @Override
    public Optional<Reservation> findById(long reservationId) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.created_at,
                    r.status,
                    rs.id AS slot_id,
                    rs.date AS slot_date,
                    ts.id AS time_id,
                    ts.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.price AS theme_price
                FROM reservation r
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                INNER JOIN time_slot ts ON rs.time_id = ts.id
                INNER JOIN theme th ON rs.theme_id = th.id
                WHERE r.id = ?
                """;
        List<Reservation> reservations = jdbcTemplate.query(sql, rowMapper(), reservationId);
        return Optional.ofNullable(DataAccessUtils.singleResult(reservations));
    }

    @Override
    public List<Reservation> findByName(String name) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.created_at,
                    r.status,
                    rs.id AS slot_id,
                    rs.date AS slot_date,
                    ts.id AS time_id,
                    ts.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.price AS theme_price
                FROM reservation r
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                INNER JOIN time_slot ts ON rs.time_id = ts.id
                INNER JOIN theme th ON rs.theme_id = th.id
                WHERE r.name = ?
                """;
        return jdbcTemplate.query(sql, rowMapper(), name);
    }

    @Override
    public List<Reservation> findBySlotId(long slotId) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.created_at,
                    r.status,
                    rs.id AS slot_id,
                    rs.date AS slot_date,
                    ts.id AS time_id,
                    ts.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.price AS theme_price
                FROM reservation r
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                INNER JOIN time_slot ts ON rs.time_id = ts.id
                INNER JOIN theme th ON rs.theme_id = th.id
                WHERE r.slot_id = ?
                """;
        return jdbcTemplate.query(sql, rowMapper(), slotId);
    }

    @Override
    public List<Reservation> findBySlotIds(List<Long> slotIds) {
        String sql = """
            SELECT
                r.id AS r_id,
                r.name,
                r.created_at,
                r.status,
                rs.id AS slot_id,
                rs.date AS slot_date,
                ts.id AS time_id,
                ts.start_at,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail_url,
                th.price AS theme_price
            FROM reservation r
            INNER JOIN reservation_slot rs ON r.slot_id = rs.id
            INNER JOIN time_slot ts ON rs.time_id = ts.id
            INNER JOIN theme th ON rs.theme_id = th.id
            WHERE r.slot_id IN (:slotIds)
            ORDER BY r.slot_id ASC, r.status ASC, r.created_at ASC, r.id ASC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotIds", slotIds);

        return namedParameterJdbcTemplate.query(sql, params, rowMapper());
    }

    @Override
    public Reservation save(Reservation reservation) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(reservation);
        long reservationId = insert.executeAndReturnKey(params).longValue();
        return new Reservation(
                reservationId,
                reservation.getName(),
                reservation.getSlot(),
                reservation.getCreatedAt(),
                reservation.getStatus()
        );
    }

    @Override
    public void deleteById(long reservationId) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, reservationId);
    }

    @Override
    public Optional<Reservation> findReservedBySlot(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.created_at,
                    r.status,
                    rs.id AS slot_id,
                    rs.date AS slot_date,
                    ts.id AS time_id,
                    ts.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.price AS theme_price
                FROM reservation r
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                INNER JOIN time_slot ts ON rs.time_id = ts.id
                INNER JOIN theme th ON rs.theme_id = th.id
                WHERE rs.date = ? 
                  AND rs.time_id = ? 
                  AND rs.theme_id = ?
                  AND r.status = ?
                """;
        return jdbcTemplate.query(sql, rowMapper(), date, timeId, themeId, ReservationStatus.RESERVED.name())
                .stream()
                .findAny();
    }

    @Override
    public void update(Reservation reservation) {
        String sql = "UPDATE reservation SET name = ?, slot_id = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                reservation.getName(),
                reservation.getSlot().getId(),
                reservation.getStatus().name(),
                reservation.getId()
        );
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        String sql = """
                SELECT count(*)
                FROM reservation r
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                WHERE rs.theme_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        String sql = """
                SELECT count(*)
                FROM reservation r
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                WHERE rs.time_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return count != null && count > 0;
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingColumns("name", "slot_id", "created_at", "status")
                .usingGeneratedKeyColumns("id");
    }

    private Map<String, Object> createParams(Reservation reservation) {
        return Map.of(
                "name", reservation.getName(),
                "slot_id", reservation.getSlot().getId(),
                "created_at", reservation.getCreatedAt(),
                "status", reservation.getStatus().name()
        );
    }

    private RowMapper<Reservation> rowMapper() {
        return (rs, rowNum) -> new Reservation(
                rs.getLong("r_id"),
                rs.getString("name"),
                new ReservationSlot(
                        rs.getLong("slot_id"),
                        rs.getObject("slot_date", LocalDate.class),
                        new TimeSlot(
                                rs.getLong("time_id"),
                                rs.getObject("start_at", LocalTime.class)
                        ),
                        new Theme(
                                rs.getLong("theme_id"),
                                rs.getString("theme_name"),
                                rs.getString("theme_description"),
                                rs.getString("theme_thumbnail_url"),
                                rs.getLong("theme_price")
                        )
                ),
                rs.getObject("created_at", LocalDateTime.class),
                ReservationStatus.valueOf(rs.getString("status"))
        );
    }
}
