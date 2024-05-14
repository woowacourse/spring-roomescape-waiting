package roomescape.dao;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.rowmapper.ReservationTimeRowMapper;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;

@Repository
public class ReservationTimeDao implements ReservationTimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final ReservationTimeRowMapper timeRowMapper;

    public ReservationTimeDao(JdbcTemplate jdbcTemplate, DataSource dataSource,
                              ReservationTimeRowMapper timeRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.timeRowMapper = timeRowMapper;
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, timeRowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        String sql = "SELECT * FROM reservation_time WHERE id = ?";
        List<ReservationTime> time = jdbcTemplate.query(sql, timeRowMapper, id);
        return DataAccessUtils.optionalResult(time);
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation_time WHERE start_at = ?)";
        return Objects.requireNonNull(jdbcTemplate.queryForObject(sql, Boolean.class, startAt));
    }

    @Override
    public ReservationTime save(ReservationTime time) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start_at", time.getStartAt());
        long id = jdbcInsert.executeAndReturnKey(parameters).longValue();
        return new ReservationTime(id, time.getStartAt());
    }

    @Override
    public void delete(ReservationTime time) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        jdbcTemplate.update(sql, time.getId());
    }
}
