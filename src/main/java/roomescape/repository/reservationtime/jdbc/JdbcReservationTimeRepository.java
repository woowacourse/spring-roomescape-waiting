package roomescape.repository.reservationtime.jdbc;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.repository.reservationtime.ReservationTimeRepository;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ReservationTime> reservationRowMapper = (resultSet, rowNumber) -> {
        long id = resultSet.getLong("id");
        LocalTime time = LocalTime.parse(resultSet.getTime("start_at").toString());
        return new ReservationTime(id, time);
    };

    @Override
    public long add(ReservationTime reservationTime) {
        String sql = "insert into reservation_time (start_at) values(?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, Time.valueOf(reservationTime.getTime()));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void deleteById(Long id) {
        String sql = "delete from reservation_time where id=?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByTime(LocalTime time) {
        String sql = "select count(id) from reservation_time where start_at = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, time) > 0;
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "select id,start_at from reservation_time where id=?";
        try {
            ReservationTime reservationTime = jdbcTemplate.queryForObject(sql, reservationRowMapper, id);
            return Optional.of(reservationTime);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "select id,start_at from reservation_time";
        return jdbcTemplate.query(sql, reservationRowMapper);
    }
}
