package roomescape.infrastructure.persistence;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.infrastructure.persistence.rowmapper.ReservationTimeRowMapper;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public Optional<ReservationTime> findById(long id) {
        String sql = "select id as reservation_time_id, start_at as reservation_time_start_at from reservation_time where id = ?";
        try {
            ReservationTime findReservationTime = jdbcTemplate.queryForObject(sql, ReservationTimeRowMapper::mapRow, id);
            return Optional.of(Objects.requireNonNull(findReservationTime));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public ReservationTime create(ReservationTime reservationTime) {
        LocalTime startAt = reservationTime.getStartAt();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", startAt);
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new ReservationTime(id, startAt);
    }

    public List<ReservationTime> findAll() {
        String sql = "select id, start_at from reservation_time";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new ReservationTime(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime()
        ));
    }

    public void deleteById(long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsByStartAt(LocalTime startAt) {
        String sql = "select exists(select 1 from reservation_time where start_at = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, startAt);
    }
}
