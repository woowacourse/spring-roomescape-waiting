package roomescape.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;

@Repository
public class SlotDao {

    private final JdbcTemplate jdbcTemplate;

    public SlotDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_SLOT_SQL = """
            SELECT s.id AS slot_id, s.date AS slot_date,
                   t.id AS time_id, t.start_at AS time_start_at,
                   th.id AS theme_id,
                   th.name AS theme_name,
                   th.description AS theme_description,
                   th.url AS theme_url
            FROM slot s
            JOIN reservation_time t ON s.time_id = t.id
            JOIN theme th ON s.theme_id = th.id
            """;

    private final RowMapper<Slot> slotRowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("time_start_at", LocalTime.class)
        );
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_url")
        );
        return Slot.restore(
                resultSet.getLong("slot_id"),
                resultSet.getObject("slot_date", LocalDate.class),
                time,
                theme
        );
    };

    public Optional<Slot> findByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        String sql = SELECT_SLOT_SQL + " WHERE s.date = ? AND s.time_id = ? AND s.theme_id = ?";
        return jdbcTemplate.query(sql, slotRowMapper, date, timeId, themeId).stream()
                .findFirst();
    }

    public Optional<Slot> findById(Long id) {
        String sql = SELECT_SLOT_SQL + " WHERE s.id = ?";
        return jdbcTemplate.query(sql, slotRowMapper, id).stream()
                .findFirst();
    }

    public Long insert(Slot slot) {
        String sql = "insert into slot(date, time_id, theme_id) values(?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setObject(1, slot.getDate());
            ps.setLong(2, slot.getTime().getId());
            ps.setLong(3, slot.getTheme().getId());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public long deleteIfNoWaiting(Long id) {
        String sql = "delete from slot where id = ? and not exists (select 1 from waiting where slot_id = ?)";
        return jdbcTemplate.update(sql, id, id);
    }
}
