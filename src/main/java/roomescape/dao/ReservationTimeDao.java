package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReservationTime> findAllReservationTimes() {
        String sql = "select id, start_at from reservation_time";
        List<ReservationTime> reservationTimeList = jdbcTemplate.query(
                sql,
                getReservationTimeRowMapper());
        return reservationTimeList;
    }

    public ReservationTime findReservationTimeById(Long id) {
        String sql = "select id, start_at from reservation_time where id = ?";
        ReservationTime reservationTime = jdbcTemplate.queryForObject(
                sql,
                getReservationTimeRowMapper(),
                id
        );
        return reservationTime;
    }

    public Long insertWithKeyHolder(LocalTime time) {
        String sql = "insert into reservation_time (start_at) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            ps.setString(1, time.toString());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();

        return id;
    }

    public int delete(Long id) {
        return jdbcTemplate.update("delete from reservation_time where id = ?", id);
    }

    public Map<ReservationTime, Boolean> findAvailableTimes(LocalDate date, Long id) {
        String sql = """
                SELECT
                    rt.id AS time_id,
                    rt.start_at,
                    NOT EXISTS (
                        SELECT 1
                        FROM reservation r
                        WHERE r.time_id = rt.id
                          AND r.theme_id = ?
                          AND r.date = ?
                    ) AS available
                FROM reservation_time rt;
                """;
        Map<ReservationTime, Boolean> reservationTimeBooleanMap = jdbcTemplate.query(sql, getMapResultSetExtractor(),
                id, date);
        return reservationTimeBooleanMap;
    }

    private RowMapper<ReservationTime> getReservationTimeRowMapper() {
        return (resultSet, rowNum) -> {
            ReservationTime reservationTime = new ReservationTime(
                    resultSet.getLong("id"),
                    LocalTime.parse(resultSet.getString("start_at"))
            );
            return reservationTime;
        };
    }

    private ResultSetExtractor<Map<ReservationTime, Boolean>> getMapResultSetExtractor() {
        return (ResultSet rs) -> {
            Map<ReservationTime, Boolean> results = new LinkedHashMap<>();

            while (rs.next()) {
                ReservationTime reservationTime = new ReservationTime(
                        rs.getLong("time_id"),
                        LocalTime.parse(rs.getString("start_at"))
                );
                boolean isAvailable = rs.getBoolean("available");
                results.put(reservationTime, isAvailable);
            }
            return results;
        };
    }
}
