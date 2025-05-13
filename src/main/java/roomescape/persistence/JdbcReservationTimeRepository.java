package roomescape.persistence;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long create(ReservationTime reservationTime) {
        String sql = "INSERT INTO reservation_time(start_at) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservationTime.startAt().toString());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<ReservationTime> findById(Long reservationTimeId) {
        try {
            String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
            ReservationTime reservationTime = jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> new ReservationTime(
                            rs.getLong("id"),
                            rs.getTime("start_at").toLocalTime()), reservationTimeId);
            return Optional.of(reservationTime);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ReservationTime(
                rs.getLong("id"),
                rs.getTime("start_at").toLocalTime())
        );
    }

    @Override
    public void deleteById(Long reservationTimeId) {
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", reservationTimeId);
    }
}
