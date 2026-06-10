package roomescape.repository;

import java.sql.PreparedStatement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationwaiting.ReservationWaiting;

@Repository
public class ReservationWaitingUpdateDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingUpdateDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(ReservationWaiting reservationWaiting) {
        String sql = "insert into waiting(name, date, time_id, theme_id, created_at) values(?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservationWaiting.getName());
            ps.setObject(2, reservationWaiting.getDate());
            ps.setLong(3, reservationWaiting.getTime().getId());
            ps.setLong(4, reservationWaiting.getTheme().getId());
            ps.setObject(5, reservationWaiting.getCreatedAt());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void delete(Long id) {
        String sql = "delete from waiting where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
