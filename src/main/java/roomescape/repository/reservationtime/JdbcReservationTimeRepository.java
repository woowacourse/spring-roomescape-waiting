package roomescape.repository.reservationtime;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.repository.PersistenceConflictException;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final String RESERVATION_TIME_BASE_SELECT = """
            SELECT rt.id,
                   rt.start_at
            FROM reservation_time AS rt
            """;

    private static final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) ->
            ReservationTime.of(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationTimeRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        String sql = "INSERT INTO reservation_time (start_at) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
                preparedStatement.setTime(1, Time.valueOf(reservationTime.getStartAt()));
                return preparedStatement;
            }, keyHolder);
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("[ERROR] 예약 ID를 생성하지 못했습니다.");
        }

        return reservationTime.withId(key.longValue());
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = RESERVATION_TIME_BASE_SELECT + " ORDER BY rt.start_at";

        return jdbcTemplate.query(sql, reservationTimeRowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(final long timeId) {
        String sql = RESERVATION_TIME_BASE_SELECT + " WHERE rt.id = ?";

        return jdbcTemplate.query(sql, reservationTimeRowMapper, timeId)
                .stream()
                .findFirst();
    }

    @Override
    public int deleteById(final long timeId) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        try {
            return jdbcTemplate.update(sql, timeId);
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                Time.valueOf(startAt)
        ));
    }

}
