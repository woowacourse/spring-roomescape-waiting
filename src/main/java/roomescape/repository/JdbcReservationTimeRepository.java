package roomescape.repository;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ReservationTime> reservationTimeRowMapper = new DataClassRowMapper<>(ReservationTime.class);

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time",
                reservationTimeRowMapper
        );
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        List<ReservationTime> result = jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time WHERE id = ?",
                reservationTimeRowMapper,
                id
        );
        return result.stream().findFirst();
    }

    @Override
    public ReservationTime save(ReservationTime time) {
        String sql = "INSERT INTO reservation_time (start_at) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, Time.valueOf(time.getStartAt()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return new ReservationTime(id, time.getStartAt());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE id = ?)",
                Boolean.class,
                id
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at = ?)",
                Boolean.class,
                Time.valueOf(startAt)
        );
        return Boolean.TRUE.equals(exists);
    }
}
