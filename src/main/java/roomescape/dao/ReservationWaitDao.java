package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.*;
import roomescape.dto.projection.WaitingResponseProjection;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

    public List<WaitingResponseProjection> findWaitingsByMemberId(Long memberId) {
        String sql = """
                SELECT
                    ranked.order_num,
                    ranked.member_id AS wait_member_id,
                    ranked.created_at,
                    r.id AS reservation_id,
                    r.member_id AS reservation_member_id,
                    r.date,
                    t.id AS time_id,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.img_url AS theme_img_url,
                    s.id AS store_id,
                    s.name AS store_name
                FROM (
                    SELECT
                        reservation_id,
                        member_id,
                        created_at,
                        id,
                        ROW_NUMBER() OVER (
                            PARTITION BY reservation_id
                            ORDER BY created_at, id
                        ) AS order_num
                    FROM reservation_wait
                ) AS ranked
                INNER JOIN reservation AS r ON ranked.reservation_id = r.id
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                INNER JOIN store AS s ON r.store_id = s.id
                WHERE ranked.member_id = ?
                ORDER BY ranked.created_at
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Reservation reservation = new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getLong("reservation_member_id"),
                    LocalDate.parse(rs.getString("date")),
                    new ReservationTime(
                            rs.getLong("time_id"),
                            LocalTime.parse(rs.getString("start_at"))
                    ),
                    rs.getLong("theme_id"),
                    rs.getLong("store_id")
            );
            Theme theme = new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_img_url")
            );
            Store store = new Store(
                    rs.getLong("store_id"),
                    rs.getString("store_name")
            );
            return new WaitingResponseProjection(
                    rs.getLong("order_num"),
                    reservation,
                    theme,
                    store,
                    rs.getLong("wait_member_id"),
                    rs.getTimestamp("created_at").toLocalDateTime()
            );
        }, memberId);
    }

    public Long findWaitOrder(Long waitId) {
        String sql = """
                SELECT ranked.order_num
                FROM (
                    SELECT
                        id,
                        ROW_NUMBER() OVER (
                            PARTITION BY reservation_id
                            ORDER BY created_at, id
                        ) AS order_num
                    FROM reservation_wait
                ) AS ranked
                WHERE ranked.id = ?
                """;
        return jdbcTemplate.queryForObject(sql, Long.class, waitId);
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
