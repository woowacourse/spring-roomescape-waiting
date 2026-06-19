package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Reservation> ROW_MAPPER = (rs, rowNum) -> {
        ReservationTime time = ReservationTime.withId(
                rs.getLong("time_id"),
                rs.getTime("time_start_at").toLocalTime()
        );
        Theme theme = Theme.withId(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );
        return Reservation.withId(
                rs.getLong("reservation_id"),
                rs.getString("reservation_name"),
                rs.getDate("reservation_date").toLocalDate(),
                time,
                theme,
                ReservationStatus.valueOf(rs.getString("reservation_status"))
        );
    };

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.status AS reservation_status,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public Reservation save(Reservation reservation) {
        String sql = "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            ps.setString(5, reservation.getStatus().name());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return Reservation.withId(
                id,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getStatus()
        );
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM reservation
                    WHERE date = ? AND time_id = ? AND theme_id = ?
                )
                """;
        Boolean result = jdbcTemplate.queryForObject(
                sql, Boolean.class, Date.valueOf(date), timeId, themeId
        );
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM reservation WHERE time_id = ?
                )
                """;
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public List<Reservation> findByNameOrderByDateAscTimeAsc(String name) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.status AS reservation_status,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.name = ?
                ORDER BY r.date ASC, t.start_at ASC
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER, name);
    }


    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.status AS reservation_status,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    @Override
    public boolean existsByDateAndTimeAndThemeExcludingId(
            LocalDate date, Long timeId, Long themeId, Long excludeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM reservation
                    WHERE date = ? AND time_id = ? AND theme_id = ? AND id != ?
                )
                """;
        Boolean result = jdbcTemplate.queryForObject(
                sql, Boolean.class, Date.valueOf(date), timeId, themeId, excludeId
        );
        return Boolean.TRUE.equals(result);
    }

    @Override
    public void updateDateAndTime(Long id, LocalDate date, Long timeId) {
        String sql = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, Date.valueOf(date), timeId, id);
    }

    @Override
    public void updateStatus(Long id, ReservationStatus status) {
        String sql = "UPDATE reservation SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), id);
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM reservation WHERE theme_id = ?
                )
                """;
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Optional<Reservation> findBySlot(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.status AS reservation_status,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.date = ? AND t.id = ? AND th.id = ?
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER, date, timeId, themeId).stream().findFirst();
    }
}
