package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.Reservations;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_BASE = """
            SELECT r.id as reservation_id, r.name, r.date,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description,
                   th.thumbnail_image_url as theme_thumbnail
            FROM reservation as r
            INNER JOIN reservation_time as t ON r.time_id = t.id
            INNER JOIN theme as th ON r.theme_id = th.id
            """;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_value").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );
        return new Reservation(
                rs.getLong("reservation_id"),
                rs.getString("name"),
                rs.getDate("date").toLocalDate(),
                time,
                theme
        );
    };

    public List<Reservation> findAll(int offset, int limit) {
        String sql = SELECT_BASE + " ORDER BY r.date DESC, time_value ASC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, reservationRowMapper, limit, offset);
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Long.class);
        return count != null ? count : 0L;
    }

    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE time_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return count != null && count > 0;
    }

    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);
        return count != null && count > 0;
    }

    public Reservation save(Reservation reservation) {
        String sql = "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return new Reservation(
                id,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }

    public Reservation update(Reservation reservation) {
        String sql = "UPDATE reservation SET date = ?, time_id = ?, theme_id = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getId()
        );
        return reservation;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    public Optional<Reservation> findById(Long id) {
        String sql = SELECT_BASE + " WHERE r.id = ?";
        List<Reservation> results = jdbcTemplate.query(sql, reservationRowMapper, id);
        return results.stream().findFirst();
    }

    public Reservations findByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = SELECT_BASE + " WHERE r.date = ? AND r.theme_id = ?";
        return new Reservations(jdbcTemplate.query(sql, reservationRowMapper, date, themeId));
    }

    public List<Reservation> findByName(String name) {
        String sql = SELECT_BASE + " WHERE r.name = ? ORDER BY r.date DESC, time_value ASC";
        return jdbcTemplate.query(sql, reservationRowMapper, name);
    }
}
