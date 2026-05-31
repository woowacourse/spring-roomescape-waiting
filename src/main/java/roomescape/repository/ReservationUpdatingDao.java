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

    public long update(Long id, Reservation reservation) {
        String sql = "update reservation set name = ?, date = ?, time_id = ?, theme_id = ?, version = ? where id = ?";
        return jdbcTemplate.update(sql,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getVersion(),
                id
        );
    }

    public long updateIfVersion(Long id, String currentVersion, Reservation reservation) {
        String sql = "update reservation set name=?, date=?, time_id=?, theme_id=?, version=? " +
                "where id=? and version=?";
        return jdbcTemplate.update(sql,
                reservation.getName(), reservation.getDate(),
                reservation.getTime().getId(), reservation.getTheme().getId(),
                reservation.getVersion(), id, currentVersion);
    }

    public void delete(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Long insert(Reservation reservation) {
        String sql = "insert into reservation(name, date, time_id, theme_id, created_at, version) values(?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setObject(2, reservation.getDate());
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            ps.setObject(5, reservation.getCreatedAt());
            ps.setString(6, reservation.getVersion());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
