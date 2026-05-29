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
import roomescape.domain.Theme;

@Repository
public class ReservationDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcReservationInsert;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            rs.getString("name"),
            rs.getDate("date").toLocalDate(),
            rs.getTimestamp("created_at").toLocalDateTime(),
            new ReservationTime(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
            new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"),
                    rs.getString("theme_thumbnail"))
    );

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcReservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
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
        long id = jdbcReservationInsert.executeAndReturnKey(Map.of(
                "name", reservation.getName(),
                "date", reservation.getDate(),
                "created_at", reservation.getCreatedAt(),
                "time_id", reservation.getTime().getId(),
                "theme_id", reservation.getTheme().getId()
        )).longValue();
        return new Reservation(id, reservation.getName(), reservation.getDate(),
                reservation.getCreatedAt(), reservation.getTime(), reservation.getTheme());
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
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class, date, timeId, themeId), 0) > 0;
    }

    public boolean existsReservationByDateAndTimeIdAndThemeIdAndName(LocalDate date, long timeId, long themeId, String username) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ?",
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
}
