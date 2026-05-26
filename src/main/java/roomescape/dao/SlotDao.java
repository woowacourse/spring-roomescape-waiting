package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Slot;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class SlotDao {

    private static final RowMapper<Slot> ROW_MAPPER = (resultSet, rowNum) ->
            new Slot(
                    resultSet.getLong("id"),
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getLong("timeId"),
                    resultSet.getLong("themeId")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public SlotDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("slot")
                .usingGeneratedKeyColumns("id");
    }

    public Slot save(Slot slot) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("date", slot.getDate());
        parameters.put("timeId", slot.getTimeId());
        parameters.put("themeId", slot.getThemeId());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);

        return slot.createWithId(generatedId.longValue());
    }

    public Optional<Slot> findByDateAndTimeAndTheme(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT s.id, 
                       s.date,
                       s.timeId,
                       s.themeId
                FROM slot AS s
                WHERE s.date = ?
                    AND s.timeId = ?
                    AND s.themeId = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, date, timeId, themeId));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }
}
