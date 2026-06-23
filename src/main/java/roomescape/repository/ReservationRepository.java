package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reserver;
import roomescape.domain.Theme;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("time_value", LocalTime.class));
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail"));
        ReservationSlot slot = new ReservationSlot(
                resultSet.getObject("date", LocalDate.class), time, theme
        );

        return new Reservation(
                resultSet.getLong("reservation_id"),
                new Reserver(resultSet.getString("username")),
                slot,
                ReservationStatus.valueOf(resultSet.getString("reservation_status"))
        );
    };

    public ReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Reservation> findAll() {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.name as username,
                    r.date,
                    r.status as reservation_status,
                    rt.id as time_id,
                    rt.start_at as time_value,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail
                FROM reservation as r
                INNER JOIN reservation_time as rt
                  ON r.time_id = rt.id
                INNER JOIN theme as t
                  ON r.theme_id = t.id;
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.name as username,
                    r.date,
                    r.status as reservation_status,
                    rt.id as time_id,
                    rt.start_at as time_value,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail
                FROM reservation as r
                INNER JOIN reservation_time as rt
                  ON r.time_id = rt.id
                INNER JOIN theme as t
                  ON r.theme_id = t.id
                WHERE r.id = ?;
                """;
        List<Reservation> result = jdbcTemplate.query(sql, reservationRowMapper, id);
        return result.stream().findAny();
    }

    public Optional<Reservation> findByIdForUpdate(Long id) {
        return lockById(id)
                .flatMap(this::findById);
    }

    public List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.name as username,
                    r.date,
                    r.status as reservation_status,
                    rt.id as time_id,
                    rt.start_at as time_value,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail
                FROM reservation as r
                JOIN reservation_time as rt ON r.time_id = rt.id
                JOIN theme as t ON r.theme_id = t.id
                WHERE r.date BETWEEN ? AND ?;
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, startDate, endDate);
    }

    public List<Reservation> findByReserver(Reserver reserver) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.name as username,
                    r.date,
                    r.status as reservation_status,
                    rt.id as time_id,
                    rt.start_at as time_value,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail
                FROM reservation as r
                INNER JOIN reservation_time as rt
                  ON r.time_id = rt.id
                INNER JOIN theme as t
                  ON r.theme_id = t.id
                WHERE r.name = ?
                ORDER BY r.id;
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, reserver.getName());
    }

    public Reservation insert(Reservation reservation) {
        ReservationSlot slot = reservation.getSlot();
        String sql = "INSERT INTO reservation(name, date, time_id, theme_id, status) VALUES (?, ?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            pstmt.setString(1, reservation.getName());
            pstmt.setObject(2, slot.getDate());
            pstmt.setLong(3, slot.getTime().getId());
            pstmt.setLong(4, slot.getTheme().getId());
            pstmt.setString(5, reservation.getStatus().name());
            return pstmt;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();
        return reservation.withId(id);
    }

    public int delete(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?;";
        return jdbcTemplate.update(sql, id);
    }

    public int update(Reservation reservation) {
        ReservationSlot slot = reservation.getSlot();
        String sql = "UPDATE reservation SET name = ?, date = ?, time_id = ?, theme_id = ? WHERE id = ?;";
        return jdbcTemplate.update(
                sql,
                reservation.getName(),
                slot.getDate(),
                slot.getTime().getId(),
                slot.getTheme().getId(),
                reservation.getId());
    }

    public boolean existsBySlot(ReservationSlot slot) {
        String sql = "SELECT count(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                slot.getDate(),
                slot.getTime().getId(),
                slot.getTheme().getId());
        return count != null && count > 0;
    }

    public boolean existsBySlotForUpdate(ReservationSlot slot) {
        String sql = """
                SELECT id
                FROM reservation
                WHERE date = ?
                  AND time_id = ?
                  AND theme_id = ?
                FOR UPDATE;
                """;

        List<Long> ids = jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> resultSet.getLong("id"),
                slot.getDate(),
                slot.getTime().getId(),
                slot.getTheme().getId());
        return !ids.isEmpty();
    }

    public boolean existsByReserverAndSlot(Reserver reserver, ReservationSlot slot) {
        String sql = "SELECT count(*) FROM reservation WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                reserver.getName(),
                slot.getDate(),
                slot.getTime().getId(),
                slot.getTheme().getId()
        );
        return count != null && count > 0;
    }

    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT count(*) FROM reservation WHERE theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);
        return count != null && count > 0;
    }

    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT count(*) FROM reservation WHERE time_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return count != null && count > 0;
    }

    private Optional<Long> lockById(Long id) {
        String sql = "SELECT id FROM reservation WHERE id = ? FOR UPDATE;";
        return jdbcTemplate.queryForList(sql, Long.class, id).stream().findFirst();
    }
}
