package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcReservationTimeDao implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert reservationTimeInserter;
    private final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    LocalTime.parse(resultSet.getString("start_at"))
            );

    public JdbcReservationTimeDao(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationTimeInserter = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        final Map<String, Object> parameters = Map.of("start_at", reservationTime.getStartAt());
        final long id = reservationTimeInserter.executeAndReturnKey(parameters).longValue();
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = "SELECT id, start_at from reservation_time";
        return jdbcTemplate.query(sql, reservationTimeRowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(final long id) {
        final String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
        return jdbcTemplate.query(sql, reservationTimeRowMapper, id).stream().findFirst();
    }

    @Override
    public void deleteById(final long id) {
        final String sql = "DELETE FROM reservation_time WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByTime(final LocalTime reservationTime) {
        final String sql = "SELECT 1 FROM reservation_time WHERE start_at = ? LIMIT 1";
        final List<Integer> result = jdbcTemplate.query(sql, (resultSet, rowNumber) -> resultSet.getInt(1),
                reservationTime);
        return !result.isEmpty();
    }
}
