package roomescape.time.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationAvailability;
import roomescape.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcReservationAvailabilityDao implements ReservationAvailabilityRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationAvailabilityDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationAvailability> findAllByDateAndTheme(final LocalDate date, final Theme theme) {
        final String sql = """
                SELECT t.id AS time_id, t.start_at AS start_at, EXISTS(
                  SELECT 1
                  FROM reservation r
                  WHERE r.time_id = t.id AND r.date = ? AND r.theme_id = ?
                ) AS is_booked
                FROM reservation_time AS t;
                """;
        RowMapper<ReservationAvailability> reservationAvailabilityRowMapper = (resultSet, rowNumber) ->
                new ReservationAvailability(
                        date,
                        new ReservationTime(resultSet.getLong("time_id"),
                                LocalTime.parse(resultSet.getString("start_at"))),
                        theme,
                        resultSet.getBoolean("is_booked")
                );
        return jdbcTemplate.query(sql, reservationAvailabilityRowMapper, date, theme.getId());
    }
}
