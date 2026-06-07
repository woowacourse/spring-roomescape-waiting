package roomescape.infra;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> {
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
        return new Reservation(
            rs.getLong("reservation_id"),
            rs.getString("name"),
            Slot.saved(
                rs.getLong("slot_id"),
                rs.getDate("date").toLocalDate(),
                time,
                theme
            )
        );
    };

    @Override
    public List<Reservation> findAll() {
        String sql = """
            SELECT r.id as reservation_id, r.name, s.id as slot_id, s.date,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
            FROM reservation as r
            INNER JOIN slot as s ON r.slot_id = s.id
            INNER JOIN reservation_time as t ON s.time_id = t.id
            INNER JOIN theme as th ON s.theme_id = th.id
            ORDER BY s.date DESC, time_value ASC;
            """;

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public List<Reservation> findByName(String name) {
        String sql = """
            SELECT r.id as reservation_id, r.name, s.id as slot_id, s.date,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
            FROM reservation as r
            INNER JOIN slot as s ON r.slot_id = s.id
            INNER JOIN reservation_time as t ON s.time_id = t.id
            INNER JOIN theme as th ON s.theme_id = th.id
            WHERE r.name = ?
            ORDER BY s.date DESC, time_value ASC;
            """;

        return jdbcTemplate.query(sql, reservationRowMapper, name);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
            SELECT r.id as reservation_id, r.name, s.id as slot_id, s.date,
                   t.id as time_id, t.start_at as time_value,
                   th.id as theme_id, th.name as theme_name,
                   th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
            FROM reservation as r
            INNER JOIN slot as s ON r.slot_id = s.id
            INNER JOIN reservation_time as t ON s.time_id = t.id
            INNER JOIN theme as th ON s.theme_id = th.id
            WHERE r.id = ?;
            """;

        List<Reservation> results = jdbcTemplate.query(sql, reservationRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public Set<Long> findReservedTimeIdsByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
            SELECT s.time_id
            FROM reservation as r
            INNER JOIN slot as s ON r.slot_id = s.id
            WHERE s.date = ? AND s.theme_id = ?;
            """;
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, date, themeId));
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = """
            SELECT COUNT(*)
            FROM reservation as r
            INNER JOIN slot as s ON r.slot_id = s.id
            WHERE s.time_id = ?
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = """
            SELECT COUNT(*)
            FROM reservation as r
            INNER JOIN slot as s ON r.slot_id = s.id
            WHERE s.theme_id = ?
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsBy(Reservation reservation) {
        String sql = """
            SELECT COUNT(*)
            FROM reservation as r
            INNER JOIN slot as s ON r.slot_id = s.id
            WHERE s.date = ? AND s.time_id = ? AND s.theme_id = ?;
            """;
        Integer count = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId()
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsBySameUser(Reservation reservation) {
        String sql = """
            SELECT COUNT(*)
            FROM reservation as r
            INNER JOIN slot as s ON r.slot_id = s.id
            WHERE r.name = ? AND s.date = ? AND s.time_id = ? AND s.theme_id = ?;
            """;
        Integer count = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId()
        );
        return count != null && count > 0;
    }

    @Override
    public Long save(Reservation reservation) {
        String sql = "INSERT INTO reservation (name, slot_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setLong(2, getSlotId(reservation));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public void updateDateTime(Reservation updated) {
        String sql = """
            UPDATE reservation
            SET slot_id = ?
            WHERE id = ?;
            """;

        jdbcTemplate.update(sql, getSlotId(updated), updated.getId());
    }

    private Long getSlotId(Reservation reservation) {
        Long slotId = reservation.getSlot().getId();
        if (slotId == null) {
            throw new IllegalArgumentException("예약을 저장하려면 슬롯 id가 필요합니다.");
        }
        return slotId;
    }
}
