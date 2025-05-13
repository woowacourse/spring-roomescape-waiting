package roomescape.reservation.repository;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.SaveException;
import roomescape.reservation.domain.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (rs, rowNum) ->
            new ReservationTime(rs.getLong("id"),
                    rs.getTime("start_at").toLocalTime());

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(final ReservationTime reservationTime) {
        final String sql = "INSERT INTO reservation_times (start_at) VALUES (?)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        final int rowAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, Time.valueOf(reservationTime.getStartAt()));
            return ps;
        }, keyHolder);

        if (rowAffected != 1) {
            throw new SaveException("예약 시간 저장에 실패했습니다.");
        }

        final Number key = keyHolder.getKey();

        return key.longValue();
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = "DELETE FROM reservation_times WHERE id = ?";
        try {
            jdbcTemplate.update(sql, id);
        } catch (final DataIntegrityViolationException e) {
            throw new DataExistException("데이터 무결성 제약으로 인해 삭제할 수 없습니다." + e);
        }
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
        final List<ReservationTime> reservationTimes = jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER, id);

        if (!reservationTimes.isEmpty()) {
            return Optional.of(reservationTimes.getFirst());
        }
        return Optional.empty();
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
