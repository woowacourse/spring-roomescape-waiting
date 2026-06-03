package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.dto.reservationtime.ReservationTimeRequest;

import java.sql.PreparedStatement;

@Repository
public class ReservationTimeUpdatingDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationTimeUpdatingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Long id, ReservationTimeRequest reservationTimeReq) {
        String sql = "update reservation_time SET start_at = ? where id = ?";
        jdbcTemplate.update(sql, reservationTimeReq.startAt(), id);
    }

    public void delete(Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Long insert(ReservationTimeRequest reservationTimeReq) {
        String sql = "insert into reservation_time(start_at) values (?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setObject(1, reservationTimeReq.startAt().toString());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
