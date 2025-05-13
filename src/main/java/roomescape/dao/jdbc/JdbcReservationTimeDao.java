package roomescape.dao.jdbc;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.exception.custom.NotFoundException;

@Repository
public class JdbcReservationTimeDao implements ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("reservation_time")
            .usingGeneratedKeyColumns("id");
    }

    public List<ReservationTime> findAllTimes() {
        String sql = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(sql, createReservationMapper());
    }

    public List<ReservationTime> findAllTimesWithBooked(LocalDate date, Long themeId) {
        String sql = """
            SELECT
              rt.id,
              rt.start_at,
              EXISTS(
                SELECT r.id
                FROM reservation as r
                WHERE r.time_id = rt.id
                    AND r.theme_id = ?
                    AND r.date = ?
              ) as alreadyBooked
            FROM reservation_time as rt
            ORDER BY rt.start_at ASC;
            """;
        return jdbcTemplate.query(sql, createReservationWithBookedMapper(), themeId, date);
    }

    public ReservationTime findTimeById(Long id) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, createReservationMapper(), id);
        } catch (DataAccessException e) {
            throw new NotFoundException("reservationTime");
        }
    }

    public boolean existTimeByStartAt(LocalTime startAt) {
        String sql = "SELECT EXISTS(SELECT id FROM reservation_time WHERE start_at = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, startAt));
    }

    public ReservationTime addTime(ReservationTime reservationTime) {
        Map<String, Object> param = new HashMap<>();
        param.put("start_at", Time.valueOf(reservationTime.getStartAt()));

        Number key = jdbcInsert.executeAndReturnKey(param);
        return new ReservationTime(key.longValue(), reservationTime.getStartAt());
    }

    public void removeTimeById(Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        int activeRow = jdbcTemplate.update(sql, id);

        if (activeRow == 0) {
            throw new NotFoundException("reservationTime");
        }
    }

    private RowMapper<ReservationTime> createReservationMapper() {
        return (rs, rowNum) -> new ReservationTime(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime());
    }

    private RowMapper<ReservationTime> createReservationWithBookedMapper() {
        return (rs, rowNum) -> new ReservationTime(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime(),
            rs.getBoolean("alreadyBooked"));
    }
}
