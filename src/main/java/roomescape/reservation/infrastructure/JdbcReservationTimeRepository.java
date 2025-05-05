package roomescape.reservation.infrastructure;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.exception.resource.InCorrectResultSizeException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeCommandRepository;
import roomescape.reservation.domain.ReservationTimeQueryRepository;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeCommandRepository, ReservationTimeQueryRepository {

    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (rs, rowNum) ->
            new ReservationTime(rs.getLong("id"),
                    rs.getTime("start_at").toLocalTime());

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(final ReservationTime reservationTime) {
        final String sql = "INSERT INTO reservation_times (start_at) VALUES (?)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, Time.valueOf(reservationTime.getStartAt()));
            return ps;
        }, keyHolder);

        return Optional.of(keyHolder.getKey())
                .map(Number::longValue)
                .orElseThrow(() -> new IllegalStateException("예약 시간 추가에 실패했습니다."));
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = "DELETE FROM reservation_times WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        final String sql = """
                SELECT
                    id,
                    start_at
                FROM reservation_times 
                WHERE id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, RESERVATION_TIME_ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new InCorrectResultSizeException("예약 시간이 여러 개 존재합니다.");
        }
    }

    @Override
    public ReservationTime getById(final Long id) {
        final String sql = """
                SELECT
                    id,
                    start_at
                FROM reservation_times 
                WHERE id = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, RESERVATION_TIME_ROW_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("예약 시간이 존재하지 않습니다.");
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new InCorrectResultSizeException("예약 시간이 여러 개 존재합니다.");
        }
    }

    @Override
    public List<ReservationTime> findAllByStartAt(final LocalTime startAt) {
        final String sql = """
                SELECT
                    id,
                    start_at
                FROM reservation_times 
                WHERE start_at = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER, startAt);
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = """
                SELECT
                    id,
                    start_at
                FROM reservation_times
                """;
        return jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER);
    }
}
