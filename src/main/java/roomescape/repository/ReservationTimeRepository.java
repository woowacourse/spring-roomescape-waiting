package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) -> new ReservationTime(
            resultSet.getLong("id"),
            resultSet.getTime("start_at").toLocalTime()
    );


    public ReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReservationTime save(ReservationTime time) {
        String sql = "INSERT INTO reservation_time " +
                "(start_at) " +
                "VALUES (?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    sql,
                    new String[]{"id"});
            ps.setTime(1, Time.valueOf(time.getStartAt()));
            return ps;
        }, keyHolder);
        return new ReservationTime(keyHolder.getKey().longValue(), time.getStartAt());
    }

    public Optional<ReservationTime> findById(long id) {
        String sql = "SELECT id, start_at " +
                "FROM reservation_time " +
                "WHERE id = ?";
        List<ReservationTime> reservationTimes = jdbcTemplate.query(sql, reservationTimeRowMapper, id);
        return reservationTimes.isEmpty() ? Optional.empty() : Optional.of(reservationTimes.get(0));
    }

    public Optional<ReservationTime> findByStartAt(LocalTime startAt) {
        String sql = "SELECT id, start_at " +
                "FROM reservation_time " +
                "WHERE start_at = ?";
        List<ReservationTime> reservationTimes = jdbcTemplate.query(sql, reservationTimeRowMapper, Time.valueOf(startAt));
        return reservationTimes.isEmpty() ? Optional.empty() : Optional.of(reservationTimes.get(0));
    }

    public List<ReservationTime> findReservedBy(LocalDate date, long themeId) {
        String sql = "SELECT rt.id, rt.start_at " +
                "FROM reservation_time AS rt " +
                "INNER JOIN reservation AS r " +
                "ON rt.id = r.reservation_time_id " +
                "WHERE r.date = ? " +
                "AND r.theme_id = ?";
        return jdbcTemplate.query(sql, reservationTimeRowMapper, Date.valueOf(date), themeId);
    }

    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at " +
                "FROM reservation_time";
        return jdbcTemplate.query(sql, reservationTimeRowMapper);
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM reservation_time " +
                "WHERE id = ?";
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, id);
            return ps;
        });
    }
}
