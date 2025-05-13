package roomescape.reservationtime;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationTimeJdbcRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationTimeJdbcRepository(
            @Autowired JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long save(final ReservationTime reservationTime) {
        final Map<String, Object> reservationTimeParameter = Map.of(
                "start_at", reservationTime.getStartAt()
        );

        return simpleJdbcInsert.executeAndReturnKey(reservationTimeParameter)
                .longValue();
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, getRowMapper());
    }

    @Override
    public ReservationTime findById(final Long id) {
        final String sql = "SELECT * FROM reservation_time WHERE id=?";
        return jdbcTemplate.queryForObject(sql, getRowMapper(), id);
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = "DELETE FROM reservation_time WHERE id=?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Boolean existsById(final Long id) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE id=?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    @Override
    public Boolean existsByStartAt(final LocalTime startAt) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at=?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, startAt);
    }

    private RowMapper<ReservationTime> getRowMapper() {
        return (resultSet, rowNum) -> {
            final ReservationTime reservationTime = new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime()
            );
            return reservationTime;
        };
    }
}
