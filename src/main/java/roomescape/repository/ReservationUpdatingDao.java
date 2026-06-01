package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;

import java.sql.PreparedStatement;

@Repository
public class ReservationUpdatingDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationUpdatingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insert(Reservation reservation) {
        String sql = "insert into reservation(slot_id, name, created_at) values(?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reservation.getSlot().getId());
            ps.setString(2, reservation.getName());
            ps.setObject(3, reservation.getCreatedAt());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public long updateName(Long id, String name) {
        String sql = "update reservation set name = ? where id = ?";
        return jdbcTemplate.update(sql, name, id);
    }

    public long delete(Long id) {
        String sql = "delete from reservation where id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
