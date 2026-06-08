package roomescape.repository;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.TimeSlot;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.repository.mapper.DomainRowMapperFactory;
import roomescape.service.dto.AvailableTimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcTimeSlotRepository implements TimeSlotRepository {

    private static final String FIND_ALL_SQL = """
            SELECT id AS t_id, start_at 
            FROM time_slot
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + " WHERE id = ?";
    private static final String FIND_BY_START_AT_SQL = FIND_ALL_SQL + " WHERE start_at = ?";
    private static final String DELETE_SQL = "DELETE FROM time_slot WHERE id = ?";
    private static final String UPDATE_SQL = "UPDATE time_slot SET start_at = ? WHERE id = ?";
    private static final String FIND_AVAILABLE_SQL = """
            SELECT
                t.id AS t_id,
                t.start_at,
                r.id IS NULL AS is_available
            FROM time_slot t
            LEFT JOIN session s ON t.id = s.time_id AND s.theme_id = ? AND s.date = ?
            LEFT JOIN reservation r ON s.id = r.session_id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<TimeSlot> rowMapper;
    private final RowMapper<AvailableTimeSlot> availableRowMapper;

    public JdbcTimeSlotRepository(JdbcTemplate jdbcTemplate, DomainRowMapperFactory mapperFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("time_slot")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = (rs, rowNum) -> mapperFactory.mapTimeSlot(rs);
        this.availableRowMapper = (rs, rowNum) -> new AvailableTimeSlot(
                mapperFactory.mapTimeSlot(rs),
                rs.getBoolean("is_available")
        );
    }

    @Override
    public List<TimeSlot> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, rowMapper);
    }

    @Override
    public Optional<TimeSlot> findById(long timeId) {
        List<TimeSlot> timeSlots = jdbcTemplate.query(FIND_BY_ID_SQL, rowMapper, timeId);
        return Optional.ofNullable(DataAccessUtils.singleResult(timeSlots));
    }

    @Override
    public Optional<TimeSlot> findByStartAt(LocalTime startAt) {
        List<TimeSlot> timeSlots = jdbcTemplate.query(FIND_BY_START_AT_SQL, rowMapper, startAt);
        return Optional.ofNullable(DataAccessUtils.singleResult(timeSlots));
    }

    @Override
    public List<AvailableTimeSlot> findAvailableTimeSlots(long themeId, LocalDate date) {
        return jdbcTemplate.query(FIND_AVAILABLE_SQL, availableRowMapper, themeId, date);
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        Map<String, Object> params = Map.of("start_at", timeSlot.getStartAt());
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new TimeSlot(id, timeSlot.getStartAt());
    }

    @Override
    public void deleteById(long timeId) {
        jdbcTemplate.update(DELETE_SQL, timeId);
    }

    @Override
    public TimeSlot update(TimeSlot timeSlot) {
        int columns = jdbcTemplate.update(UPDATE_SQL, timeSlot.getStartAt(), timeSlot.getId());
        checkUpdateResult(columns, timeSlot.getId());
        return timeSlot;
    }

    private void checkUpdateResult(int columns, long id) {
        if (columns == 0) {
            throw new TimeSlotNotFoundException(id);
        }
    }
}
