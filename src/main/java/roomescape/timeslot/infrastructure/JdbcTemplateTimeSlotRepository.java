package roomescape.timeslot.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.jdbc.JdbcUtils;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotId;
import roomescape.timeslot.domain.ReservationTime;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcTemplateTimeSlotRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<TimeSlot> reservationTimeMapper = (resultSet, rowNum) ->
            TimeSlot.withId(
                    TimeSlotId.from(resultSet.getLong("id")),
                    ReservationTime.from(resultSet.getTime("start_at").toLocalTime()));

    public boolean existsById(final TimeSlotId id) {
        final String sql = """
                select exists
                    (select 1 from time_slots where id = ?)
                """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, id.getValue()));
    }

    public boolean existsByStartAt(final LocalTime startAt) {
        final String sql = """
                select exists
                    (select 1 from time_slots where start_at = ?)
                """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, startAt));
    }

    public Optional<TimeSlot> findById(final TimeSlotId id) {
        final String sql = "select id, start_at from time_slots where id = ?";
        return JdbcUtils.queryForOptional(jdbcTemplate, sql, reservationTimeMapper, id.getValue());
    }

    public List<TimeSlot> findAll() {
        final String sql = "select id, start_at from time_slots";

        return jdbcTemplate.query(sql, reservationTimeMapper).stream()
                .toList();
    }

    public TimeSlot save(final TimeSlot timeSlot) {
        final String sql = "insert into time_slots (start_at) values (?)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setTime(1, Time.valueOf(timeSlot.getStartAt().getValue()));

            return preparedStatement;
        }, keyHolder);

        final long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        return TimeSlot.withId(TimeSlotId.from(generatedId), timeSlot.getStartAt());
    }

    public void deleteById(final TimeSlotId id) {
        final String sql = "delete from time_slots where id = ?";
        jdbcTemplate.update(sql, id.getValue());
    }
}
