package roomescape.test_config.fixture;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class SQLFixtureGenerator {

    private final JdbcTemplate jdbcTemplate;

    public SQLFixtureGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Reservation insertReservation(
            String guestName, LocalDate date, ReservationTime time, Theme theme, Status status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (guest_name, date, time_id, theme_id, status, last_modified_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, guestName);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, time.getId());
            preparedStatement.setLong(4, theme.getId());
            preparedStatement.setString(5, status.toString());
            preparedStatement.setString(6, now.toString());
            return preparedStatement;
        }, keyHolder);

        return Reservation.of(getGeneratedId(keyHolder), guestName, date, time, theme, status, now);
    }

    public void insertDeletedReservation(String guestName, LocalDate date, ReservationTime time, Theme theme) {
        insertReservation(guestName, date, time, theme, Status.CANCELED);
    }

    public ReservationTime insertReservationTime(LocalTime startAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation_time (start_at)
                    VALUES (?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, startAt.toString());
            return preparedStatement;
        }, keyHolder);

        return ReservationTime.of(getGeneratedId(keyHolder), startAt);
    }

    public ReservationTime insertDeletedReservationTime(LocalTime startAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation_time (start_at, deleted_at)
                    VALUES (?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, startAt.toString());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            return preparedStatement;
        }, keyHolder);

        return ReservationTime.of(getGeneratedId(keyHolder), startAt);
    }

    public Theme insertTheme(String name, String description, String thumbnail) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, thumbnail);
            return preparedStatement;
        }, keyHolder);

        return Theme.of(getGeneratedId(keyHolder), name, description, thumbnail);
    }

    public Theme insertDeletedTheme(String name, String description, String thumbnail) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail, deleted_at)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, thumbnail);
            preparedStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            return preparedStatement;
        }, keyHolder);

        return Theme.of(getGeneratedId(keyHolder), name, description, thumbnail);
    }

    private Long getGeneratedId(KeyHolder keyHolder) {
        return keyHolder.getKey().longValue();
    }
}
