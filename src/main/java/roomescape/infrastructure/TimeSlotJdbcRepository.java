package roomescape.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.TimeSlot;
import roomescape.domain.repository.TimeSlotRepository;

@Repository
public class TimeSlotJdbcRepository implements TimeSlotRepository {

    private final JdbcTemplate jdbcTemplate;

    public TimeSlotJdbcRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<TimeSlot> findById(final long id) {
        var sql = "SELECT * FROM RESERVATION_TIME WHERE id = ?";
        var timeSlotList = jdbcTemplate.query(sql, RowMappers.TIME_SLOT, id);

        return timeSlotList.stream().findAny();
    }

    @Override
    public long save(final TimeSlot timeSlot) {
        var insert = new SimpleJdbcInsert(jdbcTemplate);

        var generatedId = insert.withTableName("reservation_time")
            .usingGeneratedKeyColumns("id")
            .executeAndReturnKey(Map.of(
                "start_at", timeSlot.startAt()
            ));
        return generatedId.longValue();
    }

    @Override
    public boolean removeById(final long id) {
        var removedRowsCount = jdbcTemplate.update("delete from RESERVATION_TIME where id = ?", id);
        return removedRowsCount > 0;
    }

    @Override
    public List<TimeSlot> findAll() {
        var sql = "SELECT * FROM RESERVATION_TIME";
        return jdbcTemplate.query(sql, RowMappers.TIME_SLOT);
    }
}
