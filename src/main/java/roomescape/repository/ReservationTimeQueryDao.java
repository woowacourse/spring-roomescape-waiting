package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationtime.AvailableReservationTime;
import roomescape.domain.reservationtime.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationTimeQueryDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationTimeQueryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("id"),
                resultSet.getObject("start_at", LocalTime.class)
        );
        return reservationTime;
    };

    private final RowMapper<AvailableReservationTime> availableReservationTimeRowMapper =(resultSet, rowNum) ->
        new AvailableReservationTime(
            resultSet.getLong("id"),
            resultSet.getObject("start_at", LocalTime.class),
            resultSet.getBoolean("available")
        );

    public Optional<ReservationTime> findReservationTimeById(long id) {
        String sql = "select id, start_at from reservation_time where id = ?";
        try {
            ReservationTime reservationTime = jdbcTemplate.queryForObject(sql, reservationTimeRowMapper, id);
            return Optional.of(reservationTime);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<ReservationTime> findAllReservationTime() {
        String sql = "select id, start_at from reservation_time";
        return jdbcTemplate.query(sql, reservationTimeRowMapper);
    }

    public List<AvailableReservationTime> findAvailableReservationTime(LocalDate date, Long themeId) {
        String sql = """
            SELECT t.id AS id, t.start_at AS start_at,
            CASE
            WHEN r.time_id IS NULL THEN true
            ELSE false
            END AS available
            FROM reservation_time t
            LEFT JOIN (
            SELECT r.time_id
            FROM reservation r
            WHERE r.date = ? AND r.theme_id = ?
            ) AS r ON r.time_id = t.id
            """;
        return jdbcTemplate.query(sql, availableReservationTimeRowMapper, date, themeId);
    }
}
