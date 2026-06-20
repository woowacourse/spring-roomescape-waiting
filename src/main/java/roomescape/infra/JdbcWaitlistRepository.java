package roomescape.infra;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.repository.WaitlistRepository;

@Repository
public class JdbcWaitlistRepository implements WaitlistRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitlistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Waitlist> wailtListRowMapper = (rs, rowNum) -> {
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
        return new Waitlist(
            rs.getLong("waitlist_id"),
            new Member(
                rs.getLong("member_id"),
                rs.getString("member_name")
            ),
            Slot.saved(
                rs.getLong("slot_id"),
                rs.getDate("date").toLocalDate(),
                time,
                theme
            ),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    };

    @Override
    public Optional<Waitlist> findById(Long id) {
        String sql = """
            SELECT w.id as waitlist_id, m.id as member_id, m.name as member_name,
                   s.id as slot_id, s.date, w.created_at,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
            FROM waitlist as w
            INNER JOIN member as m ON w.member_id = m.id
            INNER JOIN slot as s ON w.slot_id = s.id
            INNER JOIN reservation_time as t ON s.time_id = t.id
            INNER JOIN theme as th ON s.theme_id = th.id
            WHERE w.id = ?;
            """;

        List<Waitlist> results = jdbcTemplate.query(sql, wailtListRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public List<Waitlist> findAll() {
        String sql = """
            SELECT w.id as waitlist_id, m.id as member_id, m.name as member_name,
                   s.id as slot_id, s.date, w.created_at,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
            FROM waitlist as w
            INNER JOIN member as m ON w.member_id = m.id
            INNER JOIN slot as s ON w.slot_id = s.id
            INNER JOIN reservation_time as t ON s.time_id = t.id
            INNER JOIN theme as th ON s.theme_id = th.id
            ORDER BY s.date DESC, t.start_at ASC, w.created_at ASC, w.id ASC;
            """;

        return jdbcTemplate.query(sql, wailtListRowMapper);
    }

    @Override
    public boolean existsBySameUser(Reservation reservation) {
        String sql = """
            SELECT COUNT(*)
            FROM waitlist as w
            INNER JOIN slot as s ON w.slot_id = s.id
            WHERE w.member_id = ? AND s.date = ? AND s.time_id = ? AND s.theme_id = ?;
            """;
        Integer count = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            getMemberId(reservation),
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId()
        );
        return count != null && count > 0;
    }

    @Override
    public Long save(Reservation reservation, LocalDateTime createdAt) {
        String sql = "INSERT INTO waitlist (member_id, created_at, slot_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, getMemberId(reservation));
            ps.setTimestamp(2, Timestamp.valueOf(createdAt));
            ps.setLong(3, getSlotId(reservation));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM waitlist WHERE id = ?", id);
    }

    @Override
    public List<Waitlist> findByName(String name) {
        String sql = """
            SELECT w.id as waitlist_id, m.id as member_id, m.name as member_name,
                   s.id as slot_id, s.date, w.created_at,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
            FROM waitlist as w
            INNER JOIN member as m ON w.member_id = m.id
            INNER JOIN slot as s ON w.slot_id = s.id
            INNER JOIN reservation_time as t ON s.time_id = t.id
            INNER JOIN theme as th ON s.theme_id = th.id
            WHERE m.name = ?
            ORDER BY s.date DESC, t.start_at ASC;
            """;

        return jdbcTemplate.query(sql, wailtListRowMapper, name);
    }

    @Override
    public List<Waitlist> findBySlotId(Long slotId) {
        String sql = """
            SELECT w.id as waitlist_id, m.id as member_id, m.name as member_name,
                   s.id as slot_id, s.date, w.created_at,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
            FROM waitlist as w
            INNER JOIN member as m ON w.member_id = m.id
            INNER JOIN slot as s ON w.slot_id = s.id
            INNER JOIN reservation_time as t ON s.time_id = t.id
            INNER JOIN theme as th ON s.theme_id = th.id
            WHERE w.slot_id = ?
            ORDER BY w.created_at ASC, w.id ASC;
            """;

        return jdbcTemplate.query(sql, wailtListRowMapper, slotId);
    }

    @Override
    public List<Waitlist> findBySlotIds(List<Long> slotIds) {
        if (slotIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(", ", Collections.nCopies(slotIds.size(), "?"));
        String sql = """
            SELECT w.id as waitlist_id, m.id as member_id, m.name as member_name,
                   s.id as slot_id, s.date, w.created_at,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
            FROM waitlist as w
            INNER JOIN member as m ON w.member_id = m.id
            INNER JOIN slot as s ON w.slot_id = s.id
            INNER JOIN reservation_time as t ON s.time_id = t.id
            INNER JOIN theme as th ON s.theme_id = th.id
            WHERE w.slot_id IN (%s)
            ORDER BY w.slot_id ASC, w.created_at ASC, w.id ASC;
            """.formatted(placeholders);

        return jdbcTemplate.query(sql, wailtListRowMapper, slotIds.toArray());
    }

    private Long getMemberId(Reservation reservation) {
        Long memberId = reservation.getMember().getId();
        if (memberId == null) {
            throw new IllegalArgumentException("대기를 저장하려면 회원 id가 필요합니다.");
        }
        return memberId;
    }

    private Long getSlotId(Reservation reservation) {
        Long slotId = reservation.getSlot().getId();
        if (slotId == null) {
            throw new IllegalArgumentException("대기를 저장하려면 슬롯 id가 필요합니다.");
        }
        return slotId;
    }
}
