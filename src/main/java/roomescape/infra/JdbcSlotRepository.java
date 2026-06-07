package roomescape.infra;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.Slot;
import roomescape.repository.SlotRepository;

@Repository
public class JdbcSlotRepository implements SlotRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Slot getOrCreate(Slot slot) {
        if (slot.getId() != null) {
            return slot;
        }
        try {
            jdbcTemplate.update("""
                INSERT INTO slot (date, time_id, theme_id)
                VALUES (?, ?, ?)
                """, slot.getDate(), slot.getTimeId(), slot.getThemeId());
        } catch (DuplicateKeyException ignored) {
        }
        Long id = jdbcTemplate.queryForObject("""
            SELECT id
            FROM slot
            WHERE date = ? AND time_id = ? AND theme_id = ?
            """, Long.class, slot.getDate(), slot.getTimeId(), slot.getThemeId());

        return Slot.saved(id, slot.getDate(), slot.getTime(), slot.getTheme());
    }

    @Override
    public void lockById(Long id) {
        jdbcTemplate.queryForObject("""
            SELECT id
            FROM slot
            WHERE id = ?
            FOR UPDATE
            """, Long.class, id);
    }
}
