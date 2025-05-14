package roomescape.dao;

import java.sql.PreparedStatement;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.model.AvailableReservationTime;
import roomescape.model.ReservationTime;

@Repository
public class ReservationTimeJdbcDao implements ReservationTimeDao {

    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    private static final RowMapper<AvailableReservationTime> AVAILABLE_TIME_ROW_MAPPER = (resultSet, rowNum) ->
            new AvailableReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime(),
                    resultSet.getBoolean("already_booked")
            );

    private final JdbcTemplate jdbcTemplate;

    public ReservationTimeJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReservationTime> findAll() {
        String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER);
    }

    public Long saveTime(ReservationTime reservationTime) {
        String sql = "INSERT INTO reservation_time (start_at) values(?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, java.sql.Time.valueOf(reservationTime.getStartAt()));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void deleteTimeById(Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Optional<ReservationTime> findById(final Long id) {
        String sql = "SELECT * FROM reservation_time WHERE id = ?";
        return jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER, id).stream().findFirst();
    }

    public boolean isDuplicatedStartAtExisted(LocalTime startAt) {
        String sql = "SELECT EXISTS (SELECT * FROM reservation_time WHERE start_at = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, startAt);
    }

    public List<AvailableReservationTime> findAvailableTimes(String date, Long themeId) {
        String sql = """
                SELECT rt.id,
                       rt.start_at,
                       EXISTS (
                           SELECT 1
                           FROM reservation r
                           WHERE r.time_id = rt.id
                             AND r.date = ?
                             AND r.theme_id = ?
                       ) AS already_booked
                FROM reservation_time rt
                """;

        return jdbcTemplate.query(sql, AVAILABLE_TIME_ROW_MAPPER, date, themeId);
    }
}
