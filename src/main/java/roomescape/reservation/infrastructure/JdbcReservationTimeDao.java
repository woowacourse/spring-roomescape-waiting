package roomescape.reservation.infrastructure;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.business.domain.ReservationTime;
import roomescape.reservation.business.repository.ReservationTimeDao;

@Repository
public class JdbcReservationTimeDao implements ReservationTimeDao {

    private static final RowMapper<ReservationTime> ROW_MAPPER = (rs, rowNum) -> new ReservationTime(
            rs.getLong("id"),
            rs.getObject("start_at", LocalTime.class)
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcReservationTimeDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        final String sql = "INSERT INTO reservation_time (start_at) VALUES (:startAt)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, new MapSqlParameterSource("startAt", reservationTime.getStartAt()), keyHolder);

        final Number key = keyHolder.getKey();
        return new ReservationTime(key.longValue(), reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = "SELECT * FROM reservation_time";

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public int deleteById(final Long id) {
        final String sql = "DELETE FROM reservation_time WHERE id = :id";
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        final String sql = "SELECT * FROM reservation_time WHERE id = :id";

        final List<ReservationTime> findReservationTime = jdbcTemplate.query(
                sql, new MapSqlParameterSource("id", id), ROW_MAPPER);
        return findReservationTime.stream().findFirst();
    }

    @Override
    public boolean existByTime(final LocalTime time) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at = :start_at)";

        return Boolean.TRUE == jdbcTemplate.queryForObject(
                sql, new MapSqlParameterSource("start_at", time), Boolean.class);
    }
}
