package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class SlotDao {

    private static final RowMapper<Slot> ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        );

        return new Slot(
                resultSet.getLong("id"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );
    };

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
        parameters.put("time_id", slot.getTime().getId());
        parameters.put("theme_id", slot.getTheme().getId());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);

        return slot.createWithId(generatedId.longValue());
    }

    public Optional<Slot> findById(Long slotId) {
        String sql = """
                SELECT s.id,
                       s.date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM slot AS s
                INNER JOIN reservation_time AS rt
                    ON s.time_id = rt.id
                INNER JOIN theme AS t
                    ON s.theme_id = t.id
                WHERE s.id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, slotId));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    public Optional<Slot> findByDateAndTimeAndTheme(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT s.id, 
                       s.date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM slot AS s
                INNER JOIN reservation_time AS rt 
                    ON s.time_id = rt.id
                INNER JOIN theme AS t 
                    ON s.theme_id = t.id
                WHERE s.date = ?
                    AND s.time_id = ?
                    AND s.theme_id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, date, timeId, themeId));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }
}
