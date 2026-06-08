package roomescape.test_config.fixture;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.ReservationToken;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class SqlFixtureGenerator {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SqlFixtureGenerator(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Reservation insertReservation(
            String guestName, ReservationSlot reservationSlot, Status status) {
        LocalDateTime now = LocalDateTime.now();

        return insertReservation(guestName, reservationSlot, status, now);
    }

    public Reservation insertReservation(
            String guestName,
            ReservationSlot reservationSlot,
            Status status,
            LocalDateTime lastModifiedAt
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        ReservationToken reservationToken = ReservationToken.from(status);

        jdbcTemplate.update("""
                        INSERT INTO reservation (
                            guest_name,
                            slot_id,
                            status,
                            last_modified_at,
                            confirm_token,
                            waiting_token
                        )
                        VALUES (:guestName, :slotId, :status, :lastModifiedAt, :confirmToken, :waitingToken)
                        """,
                new MapSqlParameterSource()
                        .addValue("guestName", guestName)
                        .addValue("slotId", reservationSlot.getId())
                        .addValue("status", status.toString())
                        .addValue("lastModifiedAt", Timestamp.valueOf(lastModifiedAt))
                        .addValue("confirmToken", reservationToken.confirmToken())
                        .addValue("waitingToken", reservationToken.waitingToken()),
                keyHolder,
                new String[]{"id"});

        return Reservation.of(getGeneratedId(keyHolder), guestName, reservationSlot, status, lastModifiedAt);
    }

    public ReservationSlot insertReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
        jdbcTemplate.update("""
                        MERGE INTO reservation_slot (date, time_id, theme_id)
                        KEY(date, time_id, theme_id)
                        VALUES (:date, :timeId, :themeId)
                        """,
                new MapSqlParameterSource()
                        .addValue("date", Date.valueOf(date))
                        .addValue("timeId", time.getId())
                        .addValue("themeId", theme.getId()));

        Long id = jdbcTemplate.queryForObject("""
                        SELECT id
                        FROM reservation_slot
                        WHERE date = :date
                          AND time_id = :timeId
                          AND theme_id = :themeId
                        """,
                new MapSqlParameterSource()
                        .addValue("date", Date.valueOf(date))
                        .addValue("timeId", time.getId())
                        .addValue("themeId", theme.getId()),
                Long.class);

        return ReservationSlot.of(id, date, time, theme);
    }

    public void insertDeletedReservation(String guestName, ReservationSlot slot) {
        insertReservation(guestName, slot, Status.CANCELED);
    }

    public ReservationTime insertReservationTime(LocalTime startAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update("""
                        INSERT INTO reservation_time (start_at)
                        VALUES (:startAt)
                        """,
                new MapSqlParameterSource("startAt", Time.valueOf(startAt)),
                keyHolder,
                new String[]{"id"});

        return ReservationTime.of(getGeneratedId(keyHolder), startAt);
    }

    public ReservationTime insertDeletedReservationTime(LocalTime startAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update("""
                        INSERT INTO reservation_time (start_at, deleted_at)
                        VALUES (:startAt, :deletedAt)
                        """,
                new MapSqlParameterSource()
                        .addValue("startAt", Time.valueOf(startAt))
                        .addValue("deletedAt", Timestamp.valueOf(LocalDateTime.now())),
                keyHolder,
                new String[]{"id"});

        return ReservationTime.of(getGeneratedId(keyHolder), startAt);
    }

    public Theme insertTheme(String name, String description, String thumbnail) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update("""
                        INSERT INTO theme (name, description, thumbnail)
                        VALUES (:name, :description, :thumbnail)
                        """,
                new MapSqlParameterSource()
                        .addValue("name", name)
                        .addValue("description", description)
                        .addValue("thumbnail", thumbnail),
                keyHolder,
                new String[]{"id"});

        return Theme.of(getGeneratedId(keyHolder), name, description, thumbnail);
    }

    public Theme insertDeletedTheme(String name, String description, String thumbnail) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update("""
                        INSERT INTO theme (name, description, thumbnail, deleted_at)
                        VALUES (:name, :description, :thumbnail, :deletedAt)
                        """,
                new MapSqlParameterSource()
                        .addValue("name", name)
                        .addValue("description", description)
                        .addValue("thumbnail", thumbnail)
                        .addValue("deletedAt", Timestamp.valueOf(LocalDateTime.now())),
                keyHolder,
                new String[]{"id"});

        return Theme.of(getGeneratedId(keyHolder), name, description, thumbnail);
    }

    private Long getGeneratedId(KeyHolder keyHolder) {
        return keyHolder.getKey().longValue();
    }
}
