package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.TimeAvailability;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ReservationTime> timeRowMapper = (resultSet, rowNum) -> new ReservationTime(
            resultSet.getLong("id"),
            resultSet.getObject("start_at", LocalTime.class)
    );

    private final RowMapper<TimeAvailability> timeAvailabilityRowMapper = (resultSet, rowNum) -> new TimeAvailability(
            timeRowMapper.mapRow(resultSet, rowNum),
            resultSet.getBoolean("available")
    );

    public ReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at FROM reservation_time;";
        return jdbcTemplate.query(sql, timeRowMapper);
    }

    public Optional<ReservationTime> findById(Long id) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?;";
        List<ReservationTime> result = jdbcTemplate.query(sql, timeRowMapper, id);
        return result.stream().findAny();
    }

    public ReservationTime insert(ReservationTime reservationTime) {
        String sql = "INSERT INTO reservation_time(start_at) VALUES (?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(
                    sql,
                    new String[]{"id"});
            pstmt.setObject(1, reservationTime.getStartAt());
            return pstmt;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return reservationTime.withId(id);
    }

    public int delete(Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?;";
        return jdbcTemplate.update(sql, id);
    }

    public List<TimeAvailability> findAvailabilitiesByThemeIdAndDate(Long themeId, LocalDate date) {
        String sql = """
            SELECT
                rt.id,
                rt.start_at,
                CASE WHEN r.id IS NULL THEN true ELSE false END AS available
            FROM reservation_time AS rt
            LEFT JOIN reservation AS r
                ON r.time_id = rt.id
               AND r.theme_id = ?
               AND r.date = ?
            ORDER BY rt.id;
            """;
        return jdbcTemplate.query(sql, timeAvailabilityRowMapper, themeId, date);
    }
}
