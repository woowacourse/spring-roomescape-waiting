package roomescape.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.model.User;

@Repository
public class JdbcReservationDao implements ReservationDao {

    private final JdbcTemplate jdbcTemplate;

    private final SimpleJdbcInsert insertActor;

    private static final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) ->
            new Reservation(
                    resultSet.getLong("reservation_id"),
                    resultSet.getDate("date").toLocalDate(),
                    new ReservationTime(
                            resultSet.getLong("time_id"),
                            resultSet.getTime("time_start_at").toLocalTime()
                    ),
                    new Theme(
                            resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("description"),
                            resultSet.getString("thumbnail")
                    ),
                    new User(
                            resultSet.getLong("user_id"),
                            resultSet.getString("user_name"),
                            Role.valueOf(resultSet.getString("role"))
                    )
            );

    private static final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    public JdbcReservationDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> getAllReservations() {
        String sql = """
                SELECT 
                    r.id AS reservation_id,
                    r.date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description,
                    th.thumbnail,
                    u.id AS user_id,
                    u.name AS user_name,
                    u.role
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                INNER JOIN users AS u ON r.user_id = u.id
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public List<Reservation> searchReservation(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        String sql = """
                SELECT 
                    r.id AS reservation_id,
                    r.date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description,
                    th.thumbnail,
                    u.id AS user_id,
                    u.name AS user_name,
                    u.role
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                INNER JOIN users AS u ON r.user_id = u.id
                WHERE th.id = ? AND u.id = ? 
                AND r.date BETWEEN ? AND ?
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, themeId, memberId, dateFrom, dateTo);
    }

    @Override
    public Reservation addReservation(Reservation reservation) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("date", String.valueOf(reservation.getDate()));
        parameters.put("time_id", String.valueOf(reservation.getTime().getId()));
        parameters.put("theme_id", String.valueOf(reservation.getTheme().getId()));
        parameters.put("user_id", String.valueOf(reservation.getMember().getId()));
        Number newId = insertActor.executeAndReturnKey(parameters);
        return new Reservation(newId.longValue(), reservation.getDate(), reservation.getTime(),
                reservation.getTheme(), reservation.getMember());
    }

    @Override
    public void deleteReservation(long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public long countReservationById(long id) {
        String sql = "SELECT count(id) FROM reservation WHERE id = ?";
        Long countReservation = jdbcTemplate.queryForObject(sql, (resultSet, ignored) -> resultSet.getLong(1), id);
        return returnZeroIfNull(countReservation);
    }

    @Override
    public long countReservationByTimeId(long timeId) {
        String sql = "SELECT count(id) FROM reservation WHERE time_id = ?";
        Long countReservation = jdbcTemplate.queryForObject(sql, (resultSet, ignored) -> resultSet.getLong(1), timeId);
        return returnZeroIfNull(countReservation);
    }

    @Override
    public long countReservationByDateAndTimeId(LocalDate date, long timeId, long themeId) {
        String sql = "SELECT count(id) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?";
        Long countReservation =
                jdbcTemplate.queryForObject(sql, (resultSet, ignored) -> resultSet.getLong(1), date, timeId, themeId);
        return returnZeroIfNull(countReservation);
    }

    @Override
    public List<ReservationTime> findReservationTimeByDateAndTheme(LocalDate date, long themeId) {
        String sql = """
                SELECT t.id AS time_id, t.start_at AS start_at
                FROM reservation AS r INNER JOIN reservation_time AS t ON r.time_id = t.id
                WHERE date = ? AND theme_id = ?
                """;
        return jdbcTemplate.query(sql, reservationTimeRowMapper, date, themeId);
    }

    private long returnZeroIfNull(Long queryResult) {
        if (queryResult == null) {
            return 0L;
        }
        return queryResult.longValue();
    }
}
