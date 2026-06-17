package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.exception.DataConflictException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;

@Repository
public class ReservationDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcReservationInsert;
    private final SimpleJdbcInsert jdbcWaitingInsert;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            rs.getString("name"),
            rs.getDate("date").toLocalDate(),
            rs.getTimestamp("created_at").toLocalDateTime(),
            new ReservationTime(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
            new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"),
                    rs.getString("theme_thumbnail"))
    );

    private final RowMapper<ReservationWaiting> reservationWaitingRowMapper = (rs, rowNum) -> new ReservationWaiting(
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

    public boolean existsReservationByDateAndTimeIdAndThemeIdAndName(LocalDate date, long timeId, long themeId, String username) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ?",
                Integer.class, date, timeId, themeId, username), 0) > 0;
    }

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcReservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
        this.jdbcWaitingInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_waiting")
                .usingGeneratedKeyColumns("id");
    }

    public List<Reservation> findAll(int page, int size) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.date, r.created_at,
                       t.id AS time_id, t.start_at AS time_value,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                ORDER BY r.date DESC, r.id DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, size, (long) page * size);
    }

    public long count() {
        return Objects.requireNonNullElse(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class), 0);
    }

    public Optional<Reservation> findById(long id) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.date, r.created_at,
                       t.id AS time_id, t.start_at AS time_value,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                WHERE r.id = ?
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, id).stream().findFirst();
    }

    public Reservation save(Reservation reservation) {
        try {
            long id = jdbcReservationInsert.executeAndReturnKey(Map.of(
                    "name", reservation.getName(),
                    "date", reservation.getDate(),
                    "created_at", reservation.getCreatedAt(),
                    "time_id", reservation.getTime().getId(),
                    "theme_id", reservation.getTheme().getId()
            )).longValue();
            return new Reservation(id, reservation.getName(), reservation.getDate(),
                    reservation.getCreatedAt(), reservation.getTime(), reservation.getTheme());
        } catch (DuplicateKeyException e) {
            throw new DataConflictException(e);
        }
    }

    public boolean existsByTimeId(long timeId) {
        String sql = """
            SELECT (
                SELECT COUNT(*)
                FROM reservation
                WHERE time_id = ?
            ) + (
                SELECT COUNT(*)
                FROM reservation_waiting
                WHERE time_id = ?
            ) + (
                SELECT COUNT(*)
                FROM reservation_payment
                WHERE time_id = ?
            )
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId, timeId, timeId);
        return Objects.requireNonNullElse(count, 0) > 0;
    }

    public boolean existsByThemeId(long themeId) {
        String sql = """
                SELECT (
                    SELECT COUNT(*)
                    FROM reservation
                    WHERE theme_id = ?
                ) + (
                    SELECT COUNT(*)
                    FROM reservation_waiting
                    WHERE theme_id = ?
                ) + (
                    SELECT COUNT(*)
                    FROM reservation_payment
                    WHERE theme_id = ?
                )
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId, themeId, themeId);
        return Objects.requireNonNullElse(count, 0) > 0;
    }

    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class, date, timeId, themeId), 0) > 0;
    }

    public Reservation update(Reservation reservation) {
        try {
            jdbcTemplate.update("UPDATE reservation SET date = ?, time_id = ? WHERE id = ?",
                    reservation.getDate(), reservation.getTime().getId(), reservation.getId());
            return findById(reservation.getId()).orElseThrow();
        } catch (DuplicateKeyException e) {
            throw new DataConflictException(e);
        }
    }

    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    public List<Reservation> findByName(String username) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.date, r.created_at,
                       t.id AS time_id, t.start_at AS time_value,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                WHERE r.name = ?
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, username);
    }

    public Reservation saveWaiting(Reservation reservation) {
        try {
            long id = jdbcWaitingInsert.executeAndReturnKey(Map.of(
                    "name", reservation.getName(),
                    "date", reservation.getDate(),
                    "created_at", reservation.getCreatedAt(),
                    "time_id", reservation.getTime().getId(),
                    "theme_id", reservation.getTheme().getId()
            )).longValue();
            return new Reservation(id, reservation.getName(), reservation.getDate(),
                    reservation.getCreatedAt(), reservation.getTime(), reservation.getTheme());
        } catch (DuplicateKeyException e) {
            throw new DataConflictException(e);
        }
    }

    public Optional<Reservation> findByWaitingId(long id) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.date, r.created_at,
                       t.id AS time_id, t.start_at AS time_value,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail
                FROM reservation_waiting AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                WHERE r.id = ?
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, id).stream().findFirst();
    }

    public void deleteWaiting(long id) {
        jdbcTemplate.update("DELETE FROM reservation_waiting WHERE id = ?", id);
    }

    public List<ReservationWaiting> findAllWaitingByName(String username) {
        String sql = """
                SELECT
                    sub.reservation_id, sub.name, sub.date, sub.created_at,
                    sub.time_id, sub.time_value,
                    sub.theme_id, sub.theme_name, sub.theme_description, sub.theme_thumbnail,
                    sub.waiting_order
                FROM (
                    SELECT r.id AS reservation_id, r.name, r.date, r.created_at,
                           t.id AS time_id, t.start_at AS time_value,
                           th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail,
                           ROW_NUMBER() OVER (
                               PARTITION BY r.date, r.theme_id, r.time_id
                               ORDER BY r.created_at ASC, r.id ASC
                           ) AS waiting_order
                    FROM reservation_waiting AS r
                    INNER JOIN reservation_time AS t ON r.time_id = t.id
                    INNER JOIN theme AS th ON r.theme_id = th.id
                ) AS sub
                WHERE sub.name = ?;
                """;
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, username);
    }

    public Optional<ReservationWaiting> findFirstWaitingBySlot(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.date, r.created_at,
                       t.id AS time_id, t.start_at AS time_value,
                       th.id AS theme_id, th.name AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail_url AS theme_thumbnail,
                       1 AS waiting_order
                FROM reservation_waiting AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ?
                ORDER BY r.created_at ASC, r.id ASC
                LIMIT 1;
                """;
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, date, timeId, themeId).stream().findFirst();
    }

    public boolean existsByDateAndTimeIdAndThemeIdAndName(LocalDate date, long timeId, long themeId, String username) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_waiting WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ?",
                Integer.class, date, timeId, themeId, username), 0) > 0;
    }
}
