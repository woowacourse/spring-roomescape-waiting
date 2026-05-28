package roomescape.repository.reservationwaiting;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationwaiting.ReservationWaiting;

@Repository
public class JdbcReservationWaitingRepository implements ReservationWaitingRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        String sql = "INSERT INTO reservation_waiting (reservation_id, name, requested_at) VALUES (?, ?, ?) ";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setLong(1, reservationWaiting.getReservation().getId());
            preparedStatement.setString(2, reservationWaiting.getName());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(reservationWaiting.getRequestAt()));
            return preparedStatement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("[ERROR] 대기 ID를 생성하지 못했습니다.");
        }

        return reservationWaiting.withId(key.longValue());
    }

    @Override
    public int deleteByIdAndName(Long id, String name) {
        String sql = "DELETE FROM reservation_waiting WHERE id = ? AND name = ?";
        return jdbcTemplate.update(sql, id, name);
    }

    @Override
    public boolean existsByReservationIdAndName(final Long reservationId, final String name) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_waiting WHERE reservation_id = ? AND name = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, reservationId, name));
    }

    @Override
    public boolean existsByReservationId(final Long reservationId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_waiting WHERE reservation_id = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, reservationId));
    }
}
