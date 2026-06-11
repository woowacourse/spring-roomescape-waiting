package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReservationWaitingDao {
    private static final RowMapper<ReservationWaiting> ROW_MAPPER = (resultSet, rowNum) -> {
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

        ReservationSlot slot = new ReservationSlot(
                resultSet.getLong("slot_id"),
                resultSet.getDate("reservation_date").toLocalDate(),
                reservationTime,
                theme
        );

        return new ReservationWaiting(
                resultSet.getLong("id"),
                resultSet.getString("waiting_name"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                slot
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final ReservationSlotDao reservationSlotDao;

    public ReservationWaitingDao(JdbcTemplate jdbcTemplate, ReservationSlotDao reservationSlotDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationSlotDao = reservationSlotDao;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_waiting")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationWaiting insert(ReservationWaiting reservationWaiting) {
        ReservationSlot slot = reservationSlotDao.findOrCreate(reservationWaiting.getSlot());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", reservationWaiting.getName());
        parameters.put("created_at", reservationWaiting.getCreatedAt());
        parameters.put("reservation_date", slot.getDate());
        parameters.put("time_id", slot.getTimeId());
        parameters.put("theme_id", slot.getThemeId());
        parameters.put("slot_id", slot.getId());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new ReservationWaiting(
                generatedId.longValue(),
                reservationWaiting.getName(),
                reservationWaiting.getCreatedAt(),
                slot
        );
    }

    public boolean existsByNameAndSlotId(String name, long slotId) {
        String sql = """
                SELECT COUNT(*) > 0
                FROM reservation_waiting
                WHERE name = ? AND slot_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, name, slotId);
    }

    public int delete(Long reservationWaitingId) {
        String sql = "DELETE FROM reservation_waiting WHERE id = ?";
        return jdbcTemplate.update(sql, reservationWaitingId);
    }

    public Optional<ReservationWaiting> selectById(Long reservationWaitingId) {
        try {
            String sql = baseSelectSql() + " WHERE rw.id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, reservationWaitingId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<ReservationWaiting> selectBySlot(ReservationSlot slot) {
        if (slot.getId() != null) {
            return selectBySlotId(slot.getId());
        }

        String sql = baseSelectSql() + """
                WHERE rs.date = ?
                AND rs.time_id = ?
                AND rs.theme_id = ?
                """;

        return jdbcTemplate.query(
                sql,
                ROW_MAPPER,
                slot.getDate(),
                slot.getTimeId(),
                slot.getThemeId()
        );
    }

    public List<ReservationWaiting> selectBySlotId(long slotId) {
        String sql = baseSelectSql() + " WHERE rw.slot_id = ?";

        return jdbcTemplate.query(sql, ROW_MAPPER, slotId);
    }

    public List<ReservationWaiting> select() {
        return jdbcTemplate.query(baseSelectSql(), ROW_MAPPER);
    }

    public List<ReservationWaiting> selectByName(String name) {
        String sql = baseSelectSql() + "WHERE rw.name = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, name);
    }

    private String baseSelectSql() {
        return """
                SELECT rw.id,
                       rw.name as waiting_name,
                       rw.created_at,
                       rs.id as slot_id,
                       rs.date as reservation_date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM reservation_waiting as rw
                INNER JOIN reservation_slot as rs
                ON rw.slot_id = rs.id
                INNER JOIN reservation_time as rt
                ON rs.time_id = rt.id
                INNER JOIN theme as t
                ON rs.theme_id = t.id
                """;
    }
}
