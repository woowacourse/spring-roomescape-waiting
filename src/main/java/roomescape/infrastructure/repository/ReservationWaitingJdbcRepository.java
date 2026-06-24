package roomescape.infrastructure.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.ReservationWaitingRepository;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.ConflictException;

@Repository
public class ReservationWaitingJdbcRepository implements ReservationWaitingRepository {

    private static final String ALREADY_WAITING = "이미 대기를 신청한 예약입니다.";

    private static final String SELECT_BASE = """
            SELECT 
                rw.id as waiting_id, rw.name as waiting_name, rw.created_at,
                rw.date,
                t.id as time_id, t.start_at as time_value,
                th.id as theme_id, th.name as theme_name,
                th.description as theme_description,
                th.thumbnail_image_url as theme_thumbnail
            FROM reservation_waiting as rw
            INNER JOIN reservation_time as t ON rw.time_id = t.id
            INNER JOIN theme as th ON rw.theme_id = th.id
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
                slot,
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    };

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        String sql = "INSERT INTO reservation_waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, reservationWaiting.getWaiter().name());
                ps.setDate(2, Date.valueOf(reservationWaiting.getSlot().date()));
                ps.setLong(3, reservationWaiting.getSlot().time().getId());
                ps.setLong(4, reservationWaiting.getSlot().theme().getId());
                ps.setTimestamp(5, Timestamp.valueOf(reservationWaiting.getCreatedAt()));
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(ALREADY_WAITING, e);
        }

        long id = keyHolder.getKey().longValue();
        return new ReservationWaiting(
                id,
                reservationWaiting.getWaiter(),
                reservationWaiting.getSlot(),
                reservationWaiting.getCreatedAt()
        );
    }

    @Override
    public boolean existsBy(Member member, Slot slot) {
        String sql = "SELECT COUNT(*) FROM reservation_waiting WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                member.name(),
                slot.date(),
                slot.time().getId(),
                slot.theme().getId()
        );
        return count != null && count > 0;
    }

    @Override
    public Optional<ReservationWaiting> findById(Long id) {
        String sql = SELECT_BASE + " WHERE rw.id = ?";
        List<ReservationWaiting> results = jdbcTemplate.query(sql, waitingRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public Optional<ReservationWaiting> findFirstBySlot(Slot slot) {
        String sql = SELECT_BASE + """
                WHERE rw.date = ? AND rw.time_id = ? AND rw.theme_id = ?
                ORDER BY rw.id ASC
                LIMIT 1
                """;

        List<ReservationWaiting> results = jdbcTemplate.query(
                sql,
                waitingRowMapper,
                slot.date(),
                slot.time().getId(),
                slot.theme().getId()
        );

        return results.stream().findFirst();
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation_waiting WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
