package roomescape.time.dao;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.time.domain.Time;

@Component
public class TimeJdbcDao implements TimeDao {

    private static final RowMapper<Time> TIME_ROW_MAPPER = (resultSet, rowNum) -> new Time(
            resultSet.getLong("id"),
            resultSet.getTime("start_at").toLocalTime());
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public TimeJdbcDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Time save(Time time) {
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(time);
        long id = jdbcInsert.executeAndReturnKey(sqlParameterSource).longValue();
        time.setIdOnSave(id);
        return time;
    }

    @Override
    public List<Time> findAllReservationTimesInOrder() {
        String findAllReservationTimeSql = "SELECT id, start_at FROM reservation_time ORDER BY start_at ASC ";

        return jdbcTemplate.query(findAllReservationTimeSql, TIME_ROW_MAPPER);
    }

    @Override
    public Optional<Time> findById(long reservationTimeId) {
        String findReservationTimeSql = "SELECT id, start_at FROM reservation_time WHERE id = ?";

        try {
            Time time = jdbcTemplate.queryForObject(findReservationTimeSql, TIME_ROW_MAPPER, reservationTimeId);
            return Optional.ofNullable(time);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Time> findByStartAt(LocalTime startAt) {
        String findByStartAtSql = "SELECT id, start_at FROM reservation_time WHERE start_at = ?";

        try {
            Time time = jdbcTemplate.queryForObject(findByStartAtSql, TIME_ROW_MAPPER, startAt);
            return Optional.ofNullable(time);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(long reservationTimeId) {
        String deleteReservationTimeSql = "DELETE FROM reservation_time WHERE id = ?";
        jdbcTemplate.update(deleteReservationTimeSql, reservationTimeId);
    }
}
