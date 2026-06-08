package roomescape.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Repository
public class ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation findById(Long id) {
        return jdbcTemplate.queryForObject("""
                    SELECT r.id, r.name, r.date, rt.id AS time_id, rt.start_at,
                    t.id AS theme_id, t.name AS theme_name, t.description, t.url,
                    r.requested_at
                    FROM reservation r
                    INNER JOIN reservation_time rt ON r.time_id = rt.id
                    INNER JOIN theme t ON r.theme_id = t.id
                    WHERE r.id = ?
                """, (rs, rowNum) -> {
            ReservationTime time = new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getTime("start_at").toLocalTime());
            Theme theme = new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("description"),
                    rs.getString("url"));
            return new Reservation(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getDate("date").toLocalDate(),
                    time,
                    theme,
                    rs.getTimestamp("requested_at").toLocalDateTime());
        }, id);
    }

    public List<Reservation> findByName(String name) {
        return jdbcTemplate.query("""
                    SELECT r.id, r.name, r.date, rt.id AS time_id, rt.start_at,
                           t.id AS theme_id, t.name AS theme_name, t.description, t.url,
                           r.requested_at
                    FROM reservation r
                    INNER JOIN reservation_time rt ON r.time_id = rt.id
                    INNER JOIN theme t ON r.theme_id = t.id
                    WHERE r.name = ?
                """, RESERVATION_ROW_MAPPER, name);
    }

    public List<Reservation> findBySlot(LocalDate date, Long timeId, Long themeId) {
        return jdbcTemplate.query("""
                    SELECT r.id, r.name, r.date, rt.id AS time_id, rt.start_at,
                           t.id AS theme_id, t.name AS theme_name, t.description, t.url,
                           r.requested_at
                    FROM reservation r
                    INNER JOIN reservation_time rt ON r.time_id = rt.id
                    INNER JOIN theme t ON r.theme_id = t.id
                    WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ?
                    ORDER BY r.requested_at
                """, RESERVATION_ROW_MAPPER, date, timeId, themeId);
    }

    public List<Reservation> findAll() {
        return jdbcTemplate.query("""
                    SELECT r.id,r.name,r.date,rt.id AS time_id, rt.start_at,
                    t.id AS theme_id, t.name AS theme_name, t.description, t.url,
                    r.requested_at
                    FROM reservation r
                    INNER JOIN reservation_time rt ON r.time_id = rt.id
                    INNER JOIN theme t ON r.theme_id = t.id;
                """, (rs, rowNum) -> {
            ReservationTime time = new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getTime("start_at").toLocalTime());
            Theme theme = new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("description"),
                    rs.getString("url"));
            return new Reservation(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getDate("date").toLocalDate(),
                    time,
                    theme,
                    rs.getTimestamp("requested_at").toLocalDateTime());
        });
    }

    public boolean existsBy(LocalDate date, Theme theme, ReservationTime time) {
        Boolean result = jdbcTemplate.queryForObject("""
                SELECT EXISTS(
                    SELECT *
                    FROM reservation
                    WHERE date = ?
                        AND time_id = ?
                        AND theme_id = ?
                )
                """, Boolean.class, date, time.getId(), theme.getId());
        return Boolean.TRUE.equals(result);
    }

    public Reservation save(Reservation reservation) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", reservation.getName());
        params.put("date", reservation.getDate());
        params.put("time_id", reservation.getTime().getId());
        params.put("theme_id", reservation.getTheme().getId());
        params.put("requested_at", reservation.getRequestedAt());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return findById(id);
    }

    public Reservation update(Long id, LocalDate date, Long timeId) {
        jdbcTemplate.update("""
                    UPDATE reservation
                    SET date = ?, time_id = ?
                    WHERE id = ?
                """, date, timeId, id);
        return findById(id);
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) -> {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime());
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("url"));
        return new Reservation(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDate("date").toLocalDate(),
                time,
                theme,
                rs.getTimestamp("requested_at").toLocalDateTime());
    };
}
