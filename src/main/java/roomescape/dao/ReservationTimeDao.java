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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.exception.time.NotFoundTimeException;

@Repository
public class ReservationTimeDao implements ReservationTimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<ReservationTime> timeRowMapper = (resultSet, rowNum) -> new ReservationTime(
            resultSet.getLong("id"),
            LocalTime.parse(resultSet.getString("start_at"))
    );

    public ReservationTimeDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, timeRowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
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
        Long id = (Long) jdbcInsert.executeAndReturnKey(parameters);
        return new ReservationTime(id, time.getStartAt());
    }

    @Override
    public void delete(ReservationTime time) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        int update = jdbcTemplate.update(sql, time.getId());
        checkRemoved(update);
    }

    private void checkRemoved(int count) {
        if (count < 1) {
            throw new NotFoundTimeException();
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM reservation_time";
        jdbcTemplate.update(sql);
    }
}
