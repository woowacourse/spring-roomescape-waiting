package roomescape.reservation.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.date.domain.ReservationDate;
import roomescape.reservation.domain.Reservation;
import roomescape.slot.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNumber) -> Reservation.load(
            resultSet.getLong("reservation_id"),
            resultSet.getString("name"),
            ReservationSlot.load(
                    resultSet.getLong("slot_id"),
                    ReservationDate.load(
                            resultSet.getLong("date_id"),
                            resultSet.getDate("date").toLocalDate(),
                            resultSet.getBoolean("date_is_active")
                    ),
                    ReservationTime.load(
                            resultSet.getLong("time_id"),
                            resultSet.getTime("start_at").toLocalTime(),
                            resultSet.getBoolean("time_is_active")
                    ),
                    Theme.load(
                            resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("description"),
                            resultSet.getString("thumbnail_url"),
                            resultSet.getBoolean("is_active")
                    )
            ),
            ReservationStatus.valueOf(resultSet.getString("status")),
            resultSet.getTimestamp("reserved_at").toLocalDateTime()
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
                SELECT
                    r.id AS reservation_id,
                    r.name,
                    r.status,
                    r.reserved_at,
                    rs.id AS slot_id,
                    d.id  AS date_id,
                    d.date,
                    d.is_active AS date_is_active,
                    t.id  AS time_id,
                    t.start_at,
                    t.is_active AS time_is_active,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description,
                    th.thumbnail_url,
                    th.is_active
                FROM reservation r
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                INNER JOIN reservation_date  d  ON rs.date_id  = d.id
                INNER JOIN reservation_time  t  ON rs.time_id  = t.id
                INNER JOIN theme             th ON rs.theme_id = th.id
                WHERE r.id = :id
                """;
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, params, reservationRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name,
                    r.status,
                    r.reserved_at,
                    rs.id AS slot_id,
                    d.id  AS date_id,
                    d.date,
                    d.is_active AS date_is_active,
                    t.id  AS time_id,
                    t.start_at,
                    t.is_active AS time_is_active,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description,
                    th.thumbnail_url,
                    th.is_active
                FROM reservation r
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                INNER JOIN reservation_date  d  ON rs.date_id  = d.id
                INNER JOIN reservation_time  t  ON rs.time_id  = t.id
                INNER JOIN theme             th ON rs.theme_id = th.id
                """;

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public List<Reservation> findReservedAndWaitingBySlot(ReservationSlot slot) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name,
                    r.status,
                    r.reserved_at,
                    rs.id AS slot_id,
                    d.id  AS date_id,
                    d.date,
                    d.is_active AS date_is_active,
                    t.id  AS time_id,
                    t.start_at,
                    t.is_active AS time_is_active,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description,
                    th.thumbnail_url,
                    th.is_active
                FROM reservation r
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                INNER JOIN reservation_date  d  ON rs.date_id  = d.id
                INNER JOIN reservation_time  t  ON rs.time_id  = t.id
                INNER JOIN theme             th ON rs.theme_id = th.id
                WHERE r.slot_id = :slotId
                  AND r.status IN ('RESERVED', 'WAITING')
                ORDER BY r.reserved_at ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", slot.getId());

        return jdbcTemplate.query(sql, params, reservationRowMapper);
    }

    @Override
    public List<Reservation> findReservedAndWaitingBySlotWithUpdate(ReservationSlot slot) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name,
                    r.status,
                    r.reserved_at,
                    rs.id AS slot_id,
                    d.id  AS date_id,
                    d.date,
                    d.is_active AS date_is_active,
                    t.id  AS time_id,
                    t.start_at,
                    t.is_active AS time_is_active,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description,
                    th.thumbnail_url,
                    th.is_active
                FROM reservation r
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                INNER JOIN reservation_date  d  ON rs.date_id  = d.id
                INNER JOIN reservation_time  t  ON rs.time_id  = t.id
                INNER JOIN theme             th ON rs.theme_id = th.id
                WHERE r.slot_id = :slotId
                  AND r.status IN ('RESERVED', 'WAITING')
                ORDER BY r.reserved_at ASC
                FOR UPDATE OF rs
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", slot.getId());

        return jdbcTemplate.query(sql, params, reservationRowMapper);
    }

    @Override
    public List<ReservationWithWaitingTurn> findMyReservationsWithWaitingTurn(String memberName) {
        String sql = """
                SELECT
                    r.id,
                    r.name,
                    d.date,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.thumbnail_url AS theme_thumbnail_url,
                    r.status,
                    r.reserved_at,
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
                INNER JOIN reservation_slot rs ON r.slot_id = rs.id
                INNER JOIN reservation_date  d  ON rs.date_id  = d.id
                INNER JOIN reservation_time  t  ON rs.time_id  = t.id
                INNER JOIN theme             th ON rs.theme_id = th.id
                WHERE r.name = :memberName
                ORDER BY r.reserved_at ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("memberName", memberName);

        return jdbcTemplate.query(sql, params, reservationWithWaitingTurnRowMapper);
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("slot_id", reservation.getSlot().getId())
                .addValue("status", reservation.getStatus().name())
                .addValue("reserved_at", reservation.getReservedAt());
        Long savedId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Reservation.load(
                savedId,
                reservation.getName(),
                reservation.getSlot(),
                reservation.getStatus(),
                reservation.getReservedAt()
        );
    }

    @Override
    public boolean existsReservedBySlot(ReservationSlot slot) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE slot_id = :slotId
                  AND status = 'RESERVED'
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", slot.getId());

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    public boolean updateStatus(Reservation reservation) {
        String sql = "UPDATE RESERVATION SET status = :status WHERE id = :id ";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reservation.getId())
                .addValue("status", reservation.getStatus().name());
        int updatedCount = jdbcTemplate.update(sql, params);
        return updatedCount > 0;
    }

    @Override
    public boolean updateSchedule(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET slot_id = :slotId
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", reservation.getSlot().getId())
                .addValue("id", reservation.getId());

        int updatedCount = jdbcTemplate.update(sql, params);
        return updatedCount > 0;
    }

    private final RowMapper<ReservationWithWaitingTurn> reservationWithWaitingTurnRowMapper = (rs, rowNum) -> {
        Long waitingTurn = rs.getObject("waiting_turn", Long.class);

        return new ReservationWithWaitingTurn(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getObject("date", LocalDate.class),
                rs.getObject("start_at", LocalTime.class),
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_thumbnail_url"),
                ReservationStatus.valueOf(rs.getString("status")),
                rs.getObject("reserved_at", LocalDateTime.class),
                waitingTurn
        );
    };

}
