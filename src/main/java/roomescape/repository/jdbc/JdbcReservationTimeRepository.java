package roomescape.repository.jdbc;

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
import roomescape.entity.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final String DEFAULT_SELECT_SQL = "select id, start_at from reservation_time";

    private final RowMapper<ReservationTime> reservationRowMapper = (resultSet, rowNumber) -> {
        long id = resultSet.getLong("id");
        LocalTime time = LocalTime.parse(resultSet.getTime("start_at").toString());
        return new ReservationTime(id, time);
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        String sql = "insert into reservation_time (start_at) values(?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, Time.valueOf(reservationTime.getTime()));
            return ps;
        }, keyHolder);

        long generatedId = keyHolder.getKey().longValue();

        return new ReservationTime(generatedId, reservationTime.getTime());
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(DEFAULT_SELECT_SQL, reservationRowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
    String sql = DEFAULT_SELECT_SQL + " where id = ?";
        try {
            ReservationTime reservationTime = jdbcTemplate.queryForObject(sql, reservationRowMapper, id);
            return Optional.of(reservationTime);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByTime(LocalTime time) {
        String sql = "select count(id) from reservation_time where start_at = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, time);
        return count != null && count > 0;
    }

    @Override
    public boolean existsReservationByTimeId(Long id) {
        String sql = """
                select count(rt.id) from reservation_time as rt
                inner join reservation as r
                on rt.id = r.time_id
                where rt.id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
