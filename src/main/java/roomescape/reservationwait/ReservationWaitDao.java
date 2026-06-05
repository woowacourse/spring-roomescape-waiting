package roomescape.reservationwait;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservationwait.dto.WaitingProjection;

@Repository
public class ReservationWaitDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReservationWait insert(ReservationWait reservationWait) {
        String sql = "INSERT INTO reservation_wait (member_id, reservation_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                conn -> {
                    PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});
                    ps.setLong(1, reservationWait.getMemberId());
                    ps.setLong(2, reservationWait.getReservationId());
                    return ps;
                }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return findReservationWaitById(id).orElseThrow();
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

    public List<WaitingProjection> findWaitingsByMemberId(Long memberId) {
        String sql = """
                SELECT
                    rw.reservation_id,
                    rw.member_id,
                    rw.created_at,
                    (SELECT COUNT(*) + 1
                     FROM reservation_wait sub
                     WHERE sub.reservation_id = rw.reservation_id
                       AND (sub.created_at < rw.created_at
                            OR (sub.created_at = rw.created_at AND sub.id < rw.id))) AS order_num
                FROM reservation_wait rw
                WHERE rw.member_id = ?
                ORDER BY rw.created_at, rw.id;
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new WaitingProjection(
                rs.getLong("order_num"),
                rs.getLong("reservation_id"),
                rs.getLong("member_id"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ), memberId);
    }

    public void deleteByReservationIdAndMemberId(Long reservationId, Long memberId) {
        String sql = "DELETE FROM reservation_wait WHERE reservation_id = ? AND member_id = ?";
        jdbcTemplate.update(sql, reservationId, memberId);
    }

    public void deleteAllByReservationId(Long reservationId) {
        String sql = "DELETE FROM reservation_wait WHERE reservation_id = ?";
        jdbcTemplate.update(sql, reservationId);
    }

    public Optional<Long> findEarliestMemberIdForUpdate(Long reservationId) {
        try {
            String sql = "SELECT member_id " +
                    "FROM reservation_wait " +
                    "WHERE reservation_id = ? " +
                    "ORDER BY created_at, id " +
                    "LIMIT 1 " +
                    "FOR UPDATE";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Long.class, reservationId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
