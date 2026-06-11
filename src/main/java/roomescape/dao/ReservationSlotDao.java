package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReservationSlotDao {
    private static final RowMapper<ReservationSlot> ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString(("description")),
                resultSet.getString("thumbnail")
        );

        return new ReservationSlot(
                resultSet.getLong("id"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationSlotDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_slot")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationSlot insert(ReservationSlot slot) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("date", slot.getDate());
        parameters.put("time_id", slot.getTimeId());
        parameters.put("theme_id", slot.getThemeId());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new ReservationSlot(generatedId.longValue(), slot.getDate(), slot.getTime(), slot.getTheme());
    }

    public ReservationSlot findOrCreate(ReservationSlot slot) {
        return selectByDateAndTimeIdAndThemeId(slot)
                .orElseGet(() -> insert(slot));
    }

    public Optional<ReservationSlot> selectByDateAndTimeIdAndThemeId(ReservationSlot slot) {
        try {
            String sql = baseSelectSql() + """
                     WHERE rs.date = ?
                       AND rs.time_id = ?
                       AND rs.theme_id = ?
                    """;
            return Optional.of(jdbcTemplate.queryForObject(
                    sql,
                    ROW_MAPPER,
                    slot.getDate(),
                    slot.getTimeId(),
                    slot.getThemeId()
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<ReservationSlot> selectById(long slotId) {
        try {
            String sql = baseSelectSql() + " WHERE rs.id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, slotId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<ReservationSlot> selectByIdForUpdate(long slotId) {
        try {
            String sql = baseSelectSql() + " WHERE rs.id = ? FOR UPDATE";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, slotId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private String baseSelectSql() {
        return """
                SELECT rs.id,
                       rs.date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM reservation_slot AS rs
                INNER JOIN reservation_time AS rt ON rs.time_id = rt.id
                INNER JOIN theme AS t ON rs.theme_id = t.id
                """;
    }
}
