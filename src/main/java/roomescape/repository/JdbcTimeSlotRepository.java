package roomescape.repository;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.TimeSlot;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.service.dto.AvailableTimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcTimeSlotRepository implements TimeSlotRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTimeSlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TimeSlot> findAll() {
        String sql = "SELECT id, start_at FROM time_slot";
        return jdbcTemplate.query(sql, rowMapper());
    }

    @Override
    public Optional<TimeSlot> findById(long timeId) {
        String sql = "SELECT id, start_at FROM time_slot where id = ?";
        List<TimeSlot> timeSlots = jdbcTemplate.query(sql, rowMapper(), timeId);
        return Optional.ofNullable(DataAccessUtils.singleResult(timeSlots));
    }

    @Override
    public Optional<TimeSlot> findByStartAt(LocalTime startAt) {
        String sql = "SELECT id, start_at FROM time_slot where start_at = ?";
        List<TimeSlot> timeSlots = jdbcTemplate.query(sql, rowMapper(), startAt);
        return Optional.ofNullable(DataAccessUtils.singleResult(timeSlots));
    }

    @Override
    public List<AvailableTimeSlot> findAvailableTimeSlots(long themeId, LocalDate date) {
        String sql = """
                SELECT t.id, t.start_at, r.id IS NULL AS is_available
                FROM time_slot t
                LEFT JOIN reservation r 
                ON t.id = r.time_id 
                       AND r.theme_id = ?
                       AND r.date = ?
                """;
        return jdbcTemplate.query(sql, availableTimeSlotRowMapper(), themeId, date);
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(timeSlot);
        long reservationId = insert.executeAndReturnKey(params).longValue();
        return new TimeSlot(reservationId, timeSlot.getStartAt());
    }

    @Override
    public void deleteById(long timeId) {
        String sql = "DELETE FROM time_slot where id = ?";
        jdbcTemplate.update(sql, timeId);
    }

    @Override
    public TimeSlot update(TimeSlot timeSlot) {
        String sql = "UPDATE time_slot SET start_at = ? WHERE id = ?";
        int columns = jdbcTemplate.update(sql, timeSlot.getStartAt(), timeSlot.getId());
        if (columns == 0) {
            throw new TimeSlotNotFoundException(timeSlot.getId());
        }
        return timeSlot;
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("time_slot")
                .usingGeneratedKeyColumns("id");
    }

    private Map<String, Object> createParams(TimeSlot timeSlot) {
        return Map.of("start_at", timeSlot.getStartAt());
    }

    private RowMapper<TimeSlot> rowMapper() {
        return (rs, rowNum) -> new TimeSlot(
                rs.getLong("id"),
                rs.getObject("start_at", LocalTime.class)
        );
    }

    private RowMapper<AvailableTimeSlot> availableTimeSlotRowMapper() {
        return (rs, rowNum) -> new AvailableTimeSlot(
                new TimeSlot(rs.getLong("id"), rs.getObject("start_at", LocalTime.class)),
                rs.getBoolean("is_available")
        );
    }
}
