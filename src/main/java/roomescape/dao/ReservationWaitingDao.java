package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;

@Repository
public class ReservationWaitingDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcWaitingInsert;

    private final RowMapper<ReservationWaiting> reservationWaitingRowMapper = (rs, rowNum) -> new ReservationWaiting(
            rs.getLong("waiting_id"),
            new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("name"),
                    rs.getDate("date").toLocalDate(),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    new ReservationTime(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
                    new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"),
                            rs.getString("theme_thumbnail"))
            ), rs.getLong("waiting_order")
    );

    public ReservationWaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcWaitingInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_waiting")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationWaiting saveWaiting(Reservation savedReservation) {
        long waitingId = jdbcWaitingInsert.executeAndReturnKey(
                Map.of("reservation_id", savedReservation.getId())
        ).longValue();
        return new ReservationWaiting(waitingId, savedReservation, 0);
    }

    public Optional<ReservationWaiting> findByWaitingId(long id) {
        String sql = """
                SELECT rw.id AS waiting_id, r.id AS reservation_id, r.name, r.date, r.created_at,
                       t.id AS time_id, t.start_at AS time_value,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail,
                       0 AS waiting_order
                FROM reservation_waiting AS rw
                INNER JOIN reservation AS r ON rw.reservation_id = r.id
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                WHERE rw.id = ?
                """;
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, id).stream().findFirst();
    }

    public void deleteWaiting(long waitingId) {
        Long reservationId = jdbcTemplate.queryForObject(
                "SELECT reservation_id FROM reservation_waiting WHERE id = ?", Long.class, waitingId);
        jdbcTemplate.update("DELETE FROM reservation_waiting WHERE id = ?", waitingId);
        if (reservationId != null) {
            jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", reservationId);
        }
    }

    public List<ReservationWaiting> findAllWaitingByName(String username) {
        String sql = """
                SELECT sub.waiting_id, sub.reservation_id, sub.name, sub.date, sub.created_at,
                       sub.time_id, sub.time_value,
                       sub.theme_id, sub.theme_name, sub.theme_description, sub.theme_thumbnail,
                       sub.waiting_order
                FROM (
                    SELECT rw.id AS waiting_id, r.id AS reservation_id, r.name, r.date, r.created_at,
                           t.id AS time_id, t.start_at AS time_value,
                           th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail,
                           ROW_NUMBER() OVER (
                               PARTITION BY r.date, r.theme_id, r.time_id
                               ORDER BY r.created_at ASC
                           ) AS waiting_order
                    FROM reservation_waiting AS rw
                    INNER JOIN reservation AS r ON rw.reservation_id = r.id
                    INNER JOIN reservation_time AS t ON r.time_id = t.id
                    INNER JOIN theme AS th ON r.theme_id = th.id
                ) AS sub
                WHERE sub.name = ?
                """;
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, username);
    }

    public boolean existsByDateAndTimeIdAndThemeIdAndName(LocalDate date, long timeId, long themeId, String username) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM reservation_waiting rw
                INNER JOIN reservation r ON rw.reservation_id = r.id
                WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ? AND r.name = ?
                """,
                Integer.class, date, timeId, themeId, username), 0) > 0;
    }
}
