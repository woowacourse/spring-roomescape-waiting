package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.model.ReservationTime;

@Repository
public class JdbcReservationTimeDao implements ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;

    private final SimpleJdbcInsert insertActor;

    private static final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    public JdbcReservationTimeDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<ReservationTime> findAllReservationTimes() {
        String sql = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(sql, reservationTimeRowMapper);
    }

    @Override
    public List<ReservationTime> findAllReservedTimes(LocalDate date, long themeId) {
        String sql = """
                SELECT t.id AS time_id, t.start_at AS start_at
                FROM reservation AS r INNER JOIN reservation_time AS t ON r.time_id = t.id
                WHERE DATE = ? AND theme_id = ?
                """;
        return jdbcTemplate.query(sql, reservationTimeRowMapper, date, themeId);
    }

    @Override
    public ReservationTime findReservationById(long id) {
        String sql = "SELECT * FROM reservation_time WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, reservationTimeRowMapper, id);
    }

    @Override
    public ReservationTime addReservationTime(ReservationTime reservationTime) {
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(reservationTime);
        Number newId = insertActor.executeAndReturnKey(parameters);
        return new ReservationTime(newId.longValue(), reservationTime.getStartAt());
    }

    @Override
    public void deleteReservationTime(long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public long countReservationTimeById(long id) {
        String sql = "SELECT count(id) FROM reservation_time WHERE id = ?";
        Long countReservationTime =
                jdbcTemplate.queryForObject(sql, (resultSet, ignored) -> resultSet.getLong(1), id);
        return returnZeroIfNull(countReservationTime);
    }

    @Override
    public long countReservationTimeByStartAt(LocalTime startAt) {
        String sql = "SELECT count(id) FROM reservation_time WHERE start_at = ?";
        Long countReservationTime =
                jdbcTemplate.queryForObject(sql, (resultSet, ignored) -> resultSet.getLong(1), startAt);
        return returnZeroIfNull(countReservationTime);
    }

    private long returnZeroIfNull(Long queryResult) {
        if (queryResult == null) {
            return 0;
        }
        return queryResult;
    }
}
