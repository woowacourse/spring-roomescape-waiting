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
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;

@Repository
public class ReservationDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcReservationInsert;

    private static final String BASE_SELECT = """
            SELECT r.id AS reservation_id, r.name, r.date, r.created_at, r.status,
                   t.id AS time_id, t.start_at AS time_value,
                   th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail
            FROM reservation AS r
            INNER JOIN reservation_time AS t ON r.time_id = t.id
            INNER JOIN theme AS th ON r.theme_id = th.id
            """;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            rs.getString("name"),
            rs.getDate("date").toLocalDate(),
            rs.getTimestamp("created_at").toLocalDateTime(),
            new ReservationTime(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
            new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"),
                    rs.getString("theme_thumbnail")),
            ReservationStatus.valueOf(rs.getString("status"))
    );

    private final RowMapper<ReservationWaiting> reservationWaitingRowMapper = (rs, rowNum) -> new ReservationWaiting(
            new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("name"),
                    rs.getDate("date").toLocalDate(),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    new ReservationTime(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
                    new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"),
                            rs.getString("theme_thumbnail")),
                    ReservationStatus.valueOf(rs.getString("status"))
            ),
            rs.getLong("waiting_order")
    );

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcReservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public List<Reservation> findAll(int page, int size) {
        String sql = BASE_SELECT + """
                WHERE r.status = 'CONFIRMED'
                ORDER BY r.date DESC, r.id DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, size, (long) page * size);
    }

    public long count() {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE status = 'CONFIRMED'",
                Integer.class), 0);
    }

    public Optional<Reservation> findById(long id) {
        String sql = BASE_SELECT + "WHERE r.id = ?";
        return jdbcTemplate.query(sql, reservationRowMapper, id).stream().findFirst();
    }

    public Reservation save(Reservation reservation) {
        long id = jdbcReservationInsert.executeAndReturnKey(Map.of(
                "name", reservation.getName(),
                "date", reservation.getDate(),
                "created_at", reservation.getCreatedAt(),
                "time_id", reservation.getTime().getId(),
                "theme_id", reservation.getTheme().getId(),
                "status", reservation.getStatus().name()
        )).longValue();
        return new Reservation(id, reservation.getName(), reservation.getDate(),
                reservation.getCreatedAt(), reservation.getTime(), reservation.getTheme(), reservation.getStatus());
    }

    public boolean existsByTimeId(long timeId) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE time_id = ?", Integer.class, timeId), 0) > 0;
    }

    public boolean existsByThemeId(long themeId) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE theme_id = ?", Integer.class, themeId), 0) > 0;
    }

    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ? AND status = 'CONFIRMED'",
                Integer.class, date, timeId, themeId), 0) > 0;
    }

    public boolean existsReservationByDateAndTimeIdAndThemeIdAndName(LocalDate date, long timeId, long themeId, String username) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ? AND status = 'CONFIRMED'",
                Integer.class, date, timeId, themeId, username), 0) > 0;
    }

    public Reservation update(Reservation reservation) {
        jdbcTemplate.update("UPDATE reservation SET date = ?, time_id = ? WHERE id = ?",
                reservation.getDate(), reservation.getTime().getId(), reservation.getId());
        return findById(reservation.getId()).orElseThrow();
    }

    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    public List<Reservation> findByName(String username) {
        String sql = BASE_SELECT + "WHERE r.name = ? AND r.status = 'CONFIRMED'";
        return jdbcTemplate.query(sql, reservationRowMapper, username);
    }

    public Optional<Reservation> findByIdForUpdate(long id) {
        String sql = BASE_SELECT + "WHERE r.id = ? FOR UPDATE";
        return jdbcTemplate.query(sql, reservationRowMapper, id).stream().findFirst();
    }

    public Optional<Reservation> findWaitingById(long id) {
        String sql = BASE_SELECT + "WHERE r.id = ? AND r.status = 'WAITING'";
        return jdbcTemplate.query(sql, reservationRowMapper, id).stream().findFirst();
    }

    public List<ReservationWaiting> findAllWaitingByName(String username) {
        String sql = """
                SELECT sub.reservation_id, sub.name, sub.date, sub.created_at, sub.status,
                       sub.time_id, sub.time_value,
                       sub.theme_id, sub.theme_name, sub.theme_description, sub.theme_thumbnail,
                       sub.waiting_order
                FROM (
                    SELECT r.id AS reservation_id, r.name, r.date, r.created_at, r.status,
                           t.id AS time_id, t.start_at AS time_value,
                           th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail,
                           ROW_NUMBER() OVER (
                               PARTITION BY r.date, r.theme_id, r.time_id
                               ORDER BY r.created_at ASC, r.id ASC
                           ) AS waiting_order
                    FROM reservation AS r
                    INNER JOIN reservation_time AS t ON r.time_id = t.id
                    INNER JOIN theme AS th ON r.theme_id = th.id
                    WHERE r.status = 'WAITING'
                ) AS sub
                WHERE sub.name = ?
                """;
        return jdbcTemplate.query(sql, reservationWaitingRowMapper, username);
    }

    public boolean existsWaitingByDateAndTimeIdAndThemeIdAndName(LocalDate date, long timeId, long themeId, String name) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ? AND status = 'WAITING'",
                Integer.class, date, timeId, themeId, name), 0) > 0;
    }

    public Optional<Reservation> findWaitingByIdForUpdate(long id) {
        String sql = BASE_SELECT + "WHERE r.id = ? AND r.status = 'WAITING' FOR UPDATE";
        return jdbcTemplate.query(sql, reservationRowMapper, id).stream().findFirst();
    }

    public Optional<Reservation> findFirstWaitingByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        String sql = BASE_SELECT + """
                WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ? AND r.status = 'WAITING'
                ORDER BY r.created_at ASC, r.id ASC
                LIMIT 1
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, date, timeId, themeId).stream().findFirst();
    }

    public Optional<Reservation> findFirstWaitingByDateAndTimeIdAndThemeIdForUpdate(LocalDate date, long timeId, long themeId) {
        String sql = BASE_SELECT + """
                WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ? AND r.status = 'WAITING'
                ORDER BY r.created_at ASC, r.id ASC
                LIMIT 1 FOR UPDATE
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, date, timeId, themeId).stream().findFirst();
    }

    public void updateStatus(long id, ReservationStatus status) {
        jdbcTemplate.update("UPDATE reservation SET status = ? WHERE id = ?", status.name(), id);
    }
}
