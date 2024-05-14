package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ReservationTimeJdbcDao implements ReservationTimeDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<ReservationTime> rowMapper = (resultSet, rowNumber) -> new ReservationTime(
            resultSet.getLong("id"),
            resultSet.getString("start_at"));

    public ReservationTimeJdbcDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        final LocalTime startAt = reservationTime.getStartAt();
        final Long id = jdbcInsert.executeAndReturnKey(Map.of("start_at", startAt)).longValue();
        return new ReservationTime(Objects.requireNonNull(id), startAt);
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        final String sql = "SELECT * FROM reservation_time WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = "DELETE FROM reservation_time WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
