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
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.ConflictException;
import roomescape.domain.exception.NotFoundException;

@Repository
public class ReservationWaitingJdbcRepository implements ReservationWaitingRepository {

    private static final String ALREADY_WAITING = "이미 대기를 신청한 예약입니다.";
    private static final String RESERVATION_NOT_FOUND_FORMAT = "ID %d번 예약을 찾을 수 없습니다.";

    private static final String SELECT_BASE = """
            SELECT 
                rw.id as waiting_id, rw.name as waiting_name, rw.created_at,
                r.date,
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
        Slot slot = new Slot(
                rs.getDate("date").toLocalDate(),
                time,
                theme
        );
        Member member = new Member(
                rs.getString("waiting_name")
        );

        return new ReservationWaiting(
                rs.getLong("waiting_id"),
                member,
                rs.getTimestamp("created_at").toLocalDateTime(),
                slot
        );
    };

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting, Long reservationId) {
        String sql = "INSERT INTO reservation_waiting (name, created_at, reservation_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, reservationWaiting.getWaiter().name());
                ps.setTimestamp(2, Timestamp.valueOf(reservationWaiting.getCreatedAt()));
                ps.setLong(3, reservationId);
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(ALREADY_WAITING, e);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException(
                    String.format(RESERVATION_NOT_FOUND_FORMAT, reservationId),
                    e
            );
        }

        long id = keyHolder.getKey().longValue();
        return new ReservationWaiting(
                id,
                reservationWaiting.getWaiter(),
                reservationWaiting.getCreatedAt(),
                reservationWaiting.getSlot()
        );
    }

    @Override
    public boolean existBy(Member member, Long reservationId) {
        String sql = "SELECT COUNT(*) FROM reservation_waiting WHERE name = ? AND reservation_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, member.name(), reservationId);
        return count != null && count > 0;
    }

    @Override
    public Optional<ReservationWaiting> findById(Long id) {
        String sql = SELECT_BASE + " WHERE rw.id = ?";
        List<ReservationWaiting> results = jdbcTemplate.query(sql, waitingRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_waiting WHERE id = ?", id);
    }
}
