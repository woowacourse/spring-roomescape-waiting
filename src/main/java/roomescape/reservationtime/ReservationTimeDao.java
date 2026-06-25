package roomescape.reservationtime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

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
                new DataClassRowMapper<>(ReservationTime.class));
        return reservationTimeList;
    }

    public ReservationTime findReservationTimeById(Long id) {
        String sql = "select id, start_at from reservation_time where id = ?";
        ReservationTime reservationTime = jdbcTemplate.queryForObject(
                sql,
                new DataClassRowMapper<>(ReservationTime.class),
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

    public Map<ReservationTime, Long> findAvailableTimes(LocalDate date, Long id) {
        String sql = """
                SELECT
                    rt.id AS time_id,
                    rt.start_at,
                    MIN(r.id) AS reservation_id
                FROM reservation_time rt
                LEFT JOIN reservation r
                    ON r.time_id = rt.id
                   AND r.theme_id = ?
                   AND r.date = ?
                GROUP BY rt.id, rt.start_at
                ORDER BY rt.id;
                """;
        return jdbcTemplate.query(sql, getMapResultSetExtractor(), id, date);
    }

    public List<ReservationTimeAvailability> findAvailableTimes(LocalDate date, Long themeId, Long storeId) {
        String sql = """
                SELECT
                    rt.id AS time_id,
                    rt.start_at,
                    r.id AS reservation_id,
                    r.status AS reservation_status
                FROM reservation_time rt
                LEFT JOIN reservation r
                    ON r.time_id = rt.id
                   AND r.theme_id = ?
                   AND r.store_id = ?
                   AND r.date = ?
                ORDER BY rt.id
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            ReservationTime reservationTime = new ReservationTime(
                    resultSet.getLong("time_id"),
                    LocalTime.parse(resultSet.getString("start_at"))
            );
            Long reservationId = resultSet.getObject("reservation_id", Long.class);
            String rawStatus = resultSet.getString("reservation_status");
            return new ReservationTimeAvailability(
                    reservationTime,
                    reservationId,
                    rawStatus == null ? null : roomescape.reservation.ReservationStatus.valueOf(rawStatus)
            );
        }, themeId, storeId, date);
    }

    private ResultSetExtractor<Map<ReservationTime, Long>> getMapResultSetExtractor() {
        return (ResultSet rs) -> {
            Map<ReservationTime, Long> results = new LinkedHashMap<>();

            while (rs.next()) {
                ReservationTime reservationTime = new ReservationTime(
                        rs.getLong("time_id"),
                        LocalTime.parse(rs.getString("start_at"))
                );
                Long reservationId = rs.getObject("reservation_id", Long.class);
                results.put(reservationTime, reservationId);
            }
            return results;
        };
    }
}
