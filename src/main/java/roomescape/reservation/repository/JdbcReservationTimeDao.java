package roomescape.reservation.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationTime;

@Repository
public class JdbcReservationTimeDao implements ReservationTimeDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<ReservationTime> reservationTimeMapper = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    LocalTime.parse(resultSet.getString("start_at"))
            );

    public JdbcReservationTimeDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        final String sql = "INSERT INTO reservation_time(start_at) VALUES(:startAt)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final SqlParameterSource parameters = new MapSqlParameterSource("startAt", reservationTime.getStartAt());
        jdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        return new ReservationTime(keyHolder.getKeyAs(Long.class), reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = "SELECT id, start_at from reservation_time";
        return jdbcTemplate.query(sql, reservationTimeMapper);
    }

    @Override
    public Optional<ReservationTime> findById(final long id) {
        final String sql = "SELECT id, start_at FROM reservation_time WHERE id = :id";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        try {
            ReservationTime reservationTime = jdbcTemplate.queryForObject(sql, parameters, reservationTimeMapper);
            return Optional.of(reservationTime);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isExistsByTime(final LocalTime reservationTime) {
        final String sql = "SELECT COUNT(*) FROM reservation_time WHERE start_at = :startAt";
        final SqlParameterSource parameters = new MapSqlParameterSource("startAt", reservationTime);
        Long count = jdbcTemplate.queryForObject(sql, parameters, Long.class);
        return count > 0;
    }

    @Override
    public void deleteById(final long id) {
        final String sql = "DELETE FROM reservation_time WHERE id = :id";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, parameters);
    }
}
