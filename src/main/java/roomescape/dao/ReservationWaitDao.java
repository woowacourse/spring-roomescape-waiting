package roomescape.dao;

import java.util.List;
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
import roomescape.dto.WaitingResponseProjection;
import roomescape.dto.response.WaitingResponse;

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

    public List<WaitingResponseProjection> findWaitingsByMemberId(Long memberId) {
        String sql = """
              SELECT
                  ranked.order_num,
                  ranked.reservation_id,
                  ranked.member_id,
                  ranked.created_at
              FROM (
                  SELECT
                      reservation_id,
                      member_id,
                      created_at,
                      ROW_NUMBER() OVER (
                          PARTITION BY reservation_id
                          ORDER BY created_at
                      ) AS order_num
                  FROM reservation_wait
              ) AS ranked
              WHERE ranked.member_id = ?
              ORDER BY ranked.created_at
              """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new WaitingResponseProjection(
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

    public Optional<Long> findEarliestMemberId(Long reservationId) {
        try {
            String sql = "SELECT member_id " +
                    "FROM reservation_wait " +
                    "WHERE reservation_id = ? " +
                    "ORDER BY created_at, id " +
                    "LIMIT 1";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Long.class, reservationId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
