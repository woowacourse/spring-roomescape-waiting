package roomescape.testSupport;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.PreparedStatement;

public class DatabaseHelper {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clear() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_waiting");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_slot");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time");
        jdbcTemplate.execute("TRUNCATE TABLE theme");
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    public void insertReservationDirectly(String name, LocalDate date, Long timeId, Long themeId) {
        Long slotId = getOrCreateSlotId(date, timeId, themeId);
        jdbcTemplate.update(
                "INSERT INTO reservation (name, slot_id) VALUES (?, ?)",
                name, slotId
        );
    }

    public void insertReservationWaitingDirectly(String name, LocalDate date, Long timeId, Long themeId) {
        Long slotId = getOrCreateSlotId(date, timeId, themeId);
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, slot_id, deleted_at) VALUES (?, ?, '1970-01-01 00:00:00')",
                name, slotId
        );
    }

    private Long getOrCreateSlotId(LocalDate date, Long timeId, Long themeId) {
        String selectSql = "SELECT id FROM reservation_slot WHERE reservation_date = ? AND time_id = ? AND theme_id = ?";
        List<Long> ids = jdbcTemplate.query(selectSql, (rs, rowNum) -> rs.getLong("id"), java.sql.Date.valueOf(date), timeId, themeId);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        String insertSql = "INSERT INTO reservation_slot (reservation_date, time_id, theme_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql, new String[]{"id"});
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setLong(2, timeId);
            ps.setLong(3, themeId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Long findFirstReservationId() {
        return jdbcTemplate.queryForObject("SELECT MIN(id) FROM reservation", Long.class);
    }
}
