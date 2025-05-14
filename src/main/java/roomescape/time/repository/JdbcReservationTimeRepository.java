package roomescape.time.repository;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.NotFoundException;
import roomescape.common.utils.ExecuteResult;
import roomescape.common.utils.JdbcUtils;
import roomescape.time.domain.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ReservationTime> reservationTimeMapper = (resultSet, rowNum) -> ReservationTime.withId(
            resultSet.getLong("id"),
            resultSet.getObject("start_at", LocalTime.class)
    );

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        final String sql = """
                select exists
                    (select 1 from reservation_time where start_at = ?)
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, startAt));
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        final String sql = "select id, start_at from reservation_time where id = ?";
        return JdbcUtils.queryForOptional(jdbcTemplate, sql, reservationTimeMapper, id);
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = "select id, start_at from reservation_time";

        return jdbcTemplate.query(sql, reservationTimeMapper).stream()
                .toList();
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        final String sql = "insert into reservation_time (start_at) values (?)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setTime(1, Time.valueOf(reservationTime.getTime()));

            return preparedStatement;
        }, keyHolder);

        final long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        return ReservationTime.withId(generatedId, reservationTime.getTime());
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = "delete from reservation_time where id = ?";
        ExecuteResult result = ExecuteResult.of(jdbcTemplate.update(sql, id));

        if (result == ExecuteResult.FAIL) {
            throw new NotFoundException("삭제할 시간을 찾을 수 없습니다.");
        }
    }
}
