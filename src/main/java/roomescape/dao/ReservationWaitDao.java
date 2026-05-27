package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationWait;

import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ReservationWaitDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createReservationWait(Long memberId, Long reservationId) {
        String sql = "INSERT INTO reservation_wait (member_id, reservation_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                conn -> {
                    PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});
                    ps.setLong(1, memberId);
                    ps.setLong(2, reservationId);
                    return ps;
                }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<ReservationWait> findReservationWaitById(Long waitId) {
        try {
            String sql = "SELECT id, member_id, reservation_id, created_at FROM reservation_wait WHERE id = ?";
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, new DataClassRowMapper<>(ReservationWait.class), waitId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
