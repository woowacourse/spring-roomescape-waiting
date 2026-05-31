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

    public void update(Long id, Reservation reservation) {
        String sql = "update reservation set date = ?, time_id = ? where id = ?";
        jdbcTemplate.update(sql, reservation.getDate(), reservation.getTime().getId(), id);
    }

    public void updateName(Long id, String name) {
        String sql = "update reservation set name = ? where id = ?";
        jdbcTemplate.update(sql, name, id);
    }

    public void delete(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Long insert(Reservation reservation) {
        String sql = "insert into reservation(name, date, time_id, theme_id, created_at) values(?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setObject(2, reservation.getDate());
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            ps.setObject(5, reservation.getCreatedAt());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
