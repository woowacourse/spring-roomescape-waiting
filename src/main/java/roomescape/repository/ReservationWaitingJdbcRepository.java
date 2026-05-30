package roomescape.repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.NotFoundException;

@Repository
public class ReservationWaitingJdbcRepository implements ReservationWaitingRepository {

    private static final String ALREADY_WAITING = "이미 대기를 신청한 예약입니다.";
    private static final String RESERVATION_NOT_FOUND_FORMAT = "ID %d번 예약을 찾을 수 없습니다.";

    private static final String SELECT_BASE = """
            SELECT rw.id as waiting_id, rw.name as waiting_name, rw.created_at,
                   (
                       SELECT COUNT(*)
                       FROM reservation_waiting as previous_rw
                       WHERE previous_rw.reservation_id = rw.reservation_id
                       AND (
                           previous_rw.created_at < rw.created_at
                           OR (previous_rw.created_at = rw.created_at AND previous_rw.id <= rw.id)
                       )
                   ) as waiting_order,
                   r.id as reservation_id, r.name as reservation_name, r.date,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description,
                   th.thumbnail_image_url as theme_thumbnail
            FROM reservation_waiting as rw
            INNER JOIN reservation as r ON rw.reservation_id = r.id
            INNER JOIN reservation_time as t ON r.time_id = t.id
            INNER JOIN theme as th ON r.theme_id = th.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ReservationWaiting> waitingRowMapper = (rs, rowNum) -> {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_value").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );
        Reservation reservation = new Reservation(
                rs.getLong("reservation_id"),
                rs.getString("reservation_name"),
                rs.getDate("date").toLocalDate(),
                time,
                theme
        );
        return new ReservationWaiting(
                rs.getLong("waiting_id"),
                rs.getString("waiting_name"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                reservation,
                rs.getInt("waiting_order")
        );
    };

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        int waitingOrder = calculateWaitingOrder(reservationWaiting);
        String sql = "INSERT INTO reservation_waiting (name, created_at, reservation_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, reservationWaiting.getName());
                ps.setTimestamp(2, Timestamp.valueOf(reservationWaiting.getCreatedAt()));
                ps.setLong(3, reservationWaiting.getReservation().getId());
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new BusinessRuleViolationException(ALREADY_WAITING, e);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException(
                    String.format(RESERVATION_NOT_FOUND_FORMAT, reservationWaiting.getReservation().getId()),
                    e
            );
        }

        long id = keyHolder.getKey().longValue();
        return new ReservationWaiting(
                id,
                reservationWaiting.getName(),
                reservationWaiting.getCreatedAt(),
                reservationWaiting.getReservation(),
                waitingOrder
        );
    }

    @Override
    public boolean existBy(String name, Long reservationId) {
        String sql = "SELECT COUNT(*) FROM reservation_waiting WHERE name = ? AND reservation_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, reservationId);
        return count != null && count > 0;
    }

    @Override
    public Optional<ReservationWaiting> findById(Long id) {
        String sql = SELECT_BASE + " WHERE rw.id = ?";
        List<ReservationWaiting> results = jdbcTemplate.query(sql, waitingRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public List<ReservationWaiting> findByName(String name) {
        String sql = SELECT_BASE + " WHERE rw.name = ? ORDER BY rw.created_at ASC, rw.id ASC";
        return jdbcTemplate.query(sql, waitingRowMapper, name);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_waiting WHERE id = ?", id);
    }

    private int calculateWaitingOrder(ReservationWaiting reservationWaiting) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation_waiting
                WHERE reservation_id = ?
                AND created_at <= ?
                """;
        Integer waitingCount = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                reservationWaiting.getReservation().getId(),
                Timestamp.valueOf(reservationWaiting.getCreatedAt())
        );

        return waitingCount != null ? waitingCount + 1 : 1;
    }
}
