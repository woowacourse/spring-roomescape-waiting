package roomescape.repository;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Slot;
import roomescape.repository.mapper.DomainRowMapperFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcSlotRepository implements SlotRepository {

    private static final String FIND_ALL_SQL = "SELECT * FROM slot";
    private static final String FIND_BY_CONDITIONS_SQL = """
            SELECT 
                s.id AS slot_id, 
                s.date, 
                t.id AS t_id, 
                t.start_at, 
                th.id AS theme_id, 
                th.name AS theme_name, 
                th.description AS theme_description, 
                th.thumbnail_url AS theme_thumbnail_url 
            FROM slot s 
            INNER JOIN time_slot t ON s.time_id = t.id 
            INNER JOIN theme th ON s.theme_id = th.id 
            WHERE s.date = ? AND s.time_id = ? AND s.theme_id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM slot WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Slot> rowMapper;

    public JdbcSlotRepository(JdbcTemplate jdbcTemplate, DomainRowMapperFactory mapperFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("slot")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = (rs, rowNum) -> mapperFactory.mapSlot(rs);
    }

    @Override
    public List<Slot> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, rowMapper);
    }

    @Override
    public Slot save(Slot slot) {
        Map<String, Object> params = Map.of(
                "date", slot.getDate(),
                "time_id", slot.getTimeSlot().getId(),
                "theme_id", slot.getTheme().getId()
        );
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Slot(id, slot.getDate(), slot.getTimeSlot(), slot.getTheme());
    }

    @Override
    public Optional<Slot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        List<Slot> slots = jdbcTemplate.query(FIND_BY_CONDITIONS_SQL, rowMapper, date, timeId, themeId);
        return Optional.ofNullable(DataAccessUtils.singleResult(slots));
    }

    @Override
    public void deleteById(long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }
}
