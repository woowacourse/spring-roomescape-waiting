package roomescape.reservation.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSlotRepository implements SlotRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void ensure(Long themeId, Long timeId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO reservation_slot (theme_id, time_id) " +
                            "VALUES (?, ?)",
                    themeId, timeId);
        } catch (DuplicateKeyException ignored) {
        }
    }

    @Override
    public void lock(Long themeId, Long timeId) {
        jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_slot " +
                        "WHERE theme_id = ? AND time_id = ? " +
                        "FOR UPDATE",
                Long.class, themeId, timeId);
    }
}
