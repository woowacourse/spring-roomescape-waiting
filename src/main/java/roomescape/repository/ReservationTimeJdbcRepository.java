package roomescape.repository;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

@Repository
public class ReservationTimeJdbcRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ReservationTime> rowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime()
        );
        return reservationTime;
    };

    public ReservationTimeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "select id, start_at from reservation_time";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<ReservationTime> findAll(int limit, int offset) {
        String sql = """
                select id, start_at
                from reservation_time
                order by id
                limit ? offset ?
                """;
        return jdbcTemplate.query(sql, rowMapper, limit, offset);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "select id, start_at from reservation_time where id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Long save(ReservationTime reservationTime) {
        String sql = "insert into reservation_time(start_at) values(?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, Time.valueOf(reservationTime.getStartAt()));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public int deleteById(Long id) {
        String sql = "delete from reservation_time where id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
