package roomescape.reservationwaiting.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservationwaiting.ReservationWaiting;

@Repository
public class JdbcReservationWaitingRepository implements ReservationWaitingRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        String sql = "INSERT INTO reservation_waiting (date, theme_id, time_id, name, requested_at) VALUES (?, ?, ?, ?, ?) ";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setDate(1, Date.valueOf(reservationWaiting.getDate()));
            preparedStatement.setLong(2, reservationWaiting.getThemeId());
            preparedStatement.setLong(3, reservationWaiting.getTimeId());
            preparedStatement.setString(4, reservationWaiting.getName());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(reservationWaiting.getRequestAt()));
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
    public boolean existsByDateAndThemeIdAndTimeIdAndName(final LocalDate date, final Long themeId, final Long timeId, final String name) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_waiting WHERE date = ? AND theme_id = ? AND time_id = ? AND name = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, Date.valueOf(date), themeId, timeId, name));
    }
}
