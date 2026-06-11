package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;

@Repository
public class SlotRepository {
    private static final String SELECT_ALL = """
            SELECT s.id  AS slot_id,
                   s.date AS slot_date,
                   rt.id  AS time_id,
                   rt.start_at,
                   t.id   AS theme_id,
                   t.name AS theme_name,
                   t.description,
                   t.thumbnail_url
            FROM slot s
            INNER JOIN reservation_time rt ON s.time_id  = rt.id
            INNER JOIN theme             t  ON s.theme_id = t.id
            """;
    private static final RowMapper<Slot> SLOT_ROW_MAPPER =
            (rs, rowNum) -> RepositoryRowMapper.slotRowMapper(rs);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public SlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("slot")
                .usingGeneratedKeyColumns("id");
    }

    public Slot save(Slot slot) {
        Map<String, Object> params = Map.of(
                "date", slot.getDate().getValue(),
                "time_id", slot.getTime().getId(),
                "theme_id", slot.getTheme().getId()
        );
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return slot.withId(generatedKey);
    }

    public Optional<Slot> findById(long id) {
        String sql = SELECT_ALL + "WHERE s.id = ?";
        List<Slot> result = jdbcTemplate.query(sql, SLOT_ROW_MAPPER, id);
        return result.stream().findFirst();
    }

    public Optional<Slot> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme) {
        String sql = SELECT_ALL + "WHERE s.date = ? AND s.time_id = ? AND s.theme_id = ?";
        List<Slot> result = jdbcTemplate.query(sql, SLOT_ROW_MAPPER, date, time.getId(), theme.getId());
        return result.stream().findFirst();
    }

    public boolean lockSlot(Slot foundSlot) {
        String sql = """
                SELECT 1
                FROM slot
                WHERE id = ?
                FOR UPDATE
                """;

        List<Long> result = jdbcTemplate.queryForList(sql, Long.class, foundSlot.getId());
        return !result.isEmpty();
    }
}
