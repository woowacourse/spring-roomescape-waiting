package roomescape.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.*;

@Repository
public class ReservationDao {
    private static final RowMapper<Reservation> ROW_MAPPER = (resultSet, rowNum) -> {
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

        return new Reservation(
                resultSet.getLong("id"),
                resultSet.getString("reservation_name"),
                slot
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation insert(Reservation reservation) {
        ReservationSlot slot = reservation.getSlot();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", reservation.getName());
        parameters.put("slot_id", slot.getId());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new Reservation(
                generatedId.longValue(),
                reservation.getName(),
                slot
        );
    }

    public List<Reservation> select() {
        return jdbcTemplate.query(baseSelectSql(), ROW_MAPPER);
    }

    public Optional<Reservation> selectById(long reservationId) {
        try {
            String sql = baseSelectSql() + " WHERE r.id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, reservationId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<Reservation> selectByName(String name) {
        String sql = baseSelectSql() + " WHERE r.name = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, name);
    }

    public List<Reservation> selectByThemeIdAndDate(long themeId, LocalDate date) {
        String sql = baseSelectSql() + " WHERE rs.theme_id = ? AND rs.date = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, themeId, date);
    }

    public boolean existsByTimeId(long timeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM reservation AS r
            INNER JOIN reservation_slot AS rs ON r.slot_id = rs.id
            WHERE rs.time_id = ?
            """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
    }

    public boolean existsByThemeId(long themeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM reservation AS r
            INNER JOIN reservation_slot AS rs ON r.slot_id = rs.id
            WHERE rs.theme_id = ?
            """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
    }

    public boolean existsBySlotIdExcluding(long slotId, long reservationId) {
        String sql = """
                SELECT COUNT(*) > 0
                FROM reservation 
                WHERE slot_id = ? AND id != ?
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, slotId, reservationId);
    }


    public boolean existsBySlotId(long slotId) {
        String sql = """
                SELECT COUNT(*) > 0 
                FROM reservation 
                WHERE slot_id = ? 
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, slotId);
    }

    public boolean existsByNameAndSlotId(String name, long slotId) {
        String sql = """
                SELECT COUNT(*) > 0
                FROM reservation
                WHERE name = ? AND slot_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, name, slotId);
    }

    public Reservation update(Long reservationId, ReservationSlot slot) {
        String sql = "UPDATE reservation SET slot_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, slot.getId(), reservationId);

        return selectById(reservationId).get();
    }

    public int delete(Long reservationId) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        return jdbcTemplate.update(sql, reservationId);
    }

    private String baseSelectSql() {
        return """
                SELECT r.id,
                       r.name as reservation_name,
                       rs.id as slot_id,
                       rs.date as reservation_date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM reservation AS r
                INNER JOIN reservation_slot AS rs ON r.slot_id = rs.id
                INNER JOIN reservation_time AS rt ON rs.time_id = rt.id
                INNER JOIN theme AS t ON rs.theme_id = t.id
                """;
    }
}
