package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.UserReservation;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll(int page, int size) {
        String sql = """
                SELECT r.id          AS reservation_id,
                       r.name        AS reservation_name,
                       r.date        AS reservation_date,
                       t.id          AS time_id,
                       t.start_at    AS time_start_at,
                       th.id    AS theme_id,
                       th.name    AS theme_name,
                       th.description    AS theme_description,
                       th.thumbnail    AS theme_thumbnail
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                ORDER BY r.id
                LIMIT ? OFFSET ?
                """;
        int offset = Math.max(page, 0) * size;
        return jdbcTemplate.query(sql, reservationRowsMapper(), size, offset);
    }

    @Override
    public Optional<Reservation> findById(long id) {
        String sql = """
                SELECT r.id          AS reservation_id,
                       r.name        AS reservation_name,
                       r.date        AS reservation_date,
                       t.id          AS time_id,
                       t.start_at    AS time_start_at,
                       th.id         AS theme_id,
                       th.name       AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail  AS theme_thumbnail
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                WHERE r.id = ?
                """;
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, reservationRowsMapper(), id);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findByName(String name, int page, int size) {
        String sql = """
                SELECT r.id          AS reservation_id,
                       r.name        AS reservation_name,
                       r.date        AS reservation_date,
                       t.id          AS time_id,
                       t.start_at    AS time_start_at,
                       th.id    AS theme_id,
                       th.name    AS theme_name,
                       th.description    AS theme_description,
                       th.thumbnail    AS theme_thumbnail
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                WHERE r.name = ?
                ORDER BY r.id
                LIMIT ? OFFSET ?
                """;
        int offset = Math.max(page, 0) * size;
        return jdbcTemplate.query(sql, reservationRowsMapper(), name, size, offset);
    }

    @Override
    public List<UserReservation> findUserReservations(String name, int page, int size) {
        String sql = """
                SELECT entry.id        AS entry_id,
                       entry.name      AS entry_name,
                       entry.date      AS entry_date,
                       entry.status    AS entry_status,
                       t.id            AS time_id,
                       t.start_at      AS time_start_at,
                       th.id           AS theme_id,
                       th.name         AS theme_name,
                       th.description  AS theme_description,
                       th.thumbnail    AS theme_thumbnail
                FROM (
                    SELECT r.id, r.name, r.date, r.time_id, r.theme_id,
                           'RESERVED' AS status
                    FROM reservation r
                    WHERE r.name = ?
                    UNION ALL
                    SELECT w.id, w.name, w.date, w.time_id, w.theme_id,
                           'WAITING'  AS status
                    FROM waiting w
                    WHERE w.name = ?
                ) entry
                JOIN reservation_time t ON entry.time_id = t.id
                JOIN theme th           ON entry.theme_id = th.id
                ORDER BY entry.date, t.start_at, entry.status, entry.id
                LIMIT ? OFFSET ?
                """;
        int offset = Math.max(page, 0) * size;
        return jdbcTemplate.query(sql, userReservationRowMapper(), name, name, size, offset);
    }

    @Override
    public Optional<Reservation> findBySchedule(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT r.id          AS reservation_id,
                       r.name        AS reservation_name,
                       r.date        AS reservation_date,
                       t.id          AS time_id,
                       t.start_at    AS time_start_at,
                       th.id         AS theme_id,
                       th.name       AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail  AS theme_thumbnail
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ?
                ORDER BY r.id
                LIMIT 1
                """;
        try {
            Reservation found = jdbcTemplate.queryForObject(sql, reservationRowsMapper(),
                    date,
                    timeId,
                    themeId);
            return Optional.ofNullable(found);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> findReserverNameByScheduleForUpdate(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT name
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ?
                FOR UPDATE
                """;
        try {
            String reserverName = jdbcTemplate.queryForObject(sql, String.class, date, timeId, themeId);
            return Optional.ofNullable(reserverName);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Reservation save(Reservation reservation) {
        String sql = "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setObject(2, reservation.getDate());
            ps.setObject(3, reservation.getTime().getId());
            ps.setObject(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        return new Reservation(
                id,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme());
    }

    @Override
    public boolean existsByTimeId(long id) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE time_id = ?";
        Integer count =  jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void update(Reservation reservation) {
        String sql = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getId());
    }

    @Override
    public void delete(Reservation reservation) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, reservation.getId());
    }

    private RowMapper<UserReservation> userReservationRowMapper() {
        return (rs, rowNum) -> {
            ReservationTime time = new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getObject("time_start_at", LocalTime.class)
            );

            Theme theme = new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail")
            );

            Long id = rs.getLong("entry_id");
            String name = rs.getString("entry_name");
            LocalDate date = LocalDate.parse(rs.getString("entry_date"));

            if ("WAITING".equals(rs.getString("entry_status"))) {
                return UserReservation.waiting(id, name, date, time, theme);
            }
            return UserReservation.from(id, name, date, time, theme);
        };
    }

    private RowMapper<Reservation> reservationRowsMapper() {
        return (rs, rowNum) -> {
            ReservationTime time = new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getObject("time_start_at", LocalTime.class)
            );

            Theme theme = new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("thumbnail")
            );

            return new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("reservation_name"),
                    LocalDate.parse(rs.getString("reservation_date")),
                    time,
                    theme
            );
        };
    }
}
