package roomescape.reservationwaiting.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.theme.domain.Theme;

import java.util.List;

@Repository
public class JdbcReservationWaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<ReservationWaiting> rowMapper = (resultSet, rowNum) -> ReservationWaiting.restore(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            Reservation.restore(
                    resultSet.getLong("reservation_id"),
                    resultSet.getString("reservation_name"),
                    resultSet.getDate("reservation_date").toLocalDate(),
                    ReservationTime.restore(
                            resultSet.getLong("time_id"),
                            resultSet.getTime("time_start_at").toLocalTime(),
                            resultSet.getTime("time_finish_at").toLocalTime()
                    ),
                    Theme.restore(
                            resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("theme_description"),
                            resultSet.getString("theme_image_url")
                    )
            )
    );

    public JdbcReservationWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_waiting")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", reservationWaiting.getName())
                .addValue("reservation_id", reservationWaiting.getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return ReservationWaiting.restore(id, reservationWaiting.getName(), reservationWaiting.getReservation());
    }

    public void deleteById(Long id) {
        String query = "delete from reservation_waiting where id = ?";
        jdbcTemplate.update(query, id);
    }

    public List<ReservationWaiting> findByName(String name) {
        String query = """
                SELECT rw.id as reservation_waiting_id, rw.name,
                       r.id as reservation_id, r.name, r.date,
                       rt.id as time_id, rt.start_at as time_start_at, rt.finish_at as time_finish_at,
                       t.id as theme_id, t.name as theme_name, t.description as theme_description, t.image_url as theme_image_url
                FROM reservation_waiting rw
                JOIN reservation r ON rw.reservation_id = r.id
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE rw.name = ?
                ORDER BY r.date DESC, rt.start_at DESC
                """;
        return jdbcTemplate.query(query, rowMapper, name);
    }
}
