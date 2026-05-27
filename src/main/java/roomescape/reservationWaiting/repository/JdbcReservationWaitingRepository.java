package roomescape.reservationWaiting.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservationWaiting.domain.ReservationWaiting;

@Repository
public class JdbcReservationWaitingRepository implements ReservationWaitingRepository{

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        String sql = """
                INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservationWaiting.getName());
            ps.setDate(2, Date.valueOf(reservationWaiting.getDate()));
            ps.setLong(3, reservationWaiting.getTime().getId());
            ps.setLong(4, reservationWaiting.getTheme().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();

        return new ReservationWaiting(
                id,
                reservationWaiting.getName(),
                reservationWaiting.getDate(),
                reservationWaiting.getTime(),
                reservationWaiting.getTheme()
        );
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeIdAndName(LocalDate date, Long timeId, Long themeId, String name) {
        String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM reservation_waiting
            WHERE reservation_date = ? AND time_id = ? AND theme_id = ? AND name = ?
        )
        """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId, name);
        return Boolean.TRUE.equals(exists);
    }
}
