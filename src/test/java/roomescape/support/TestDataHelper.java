package roomescape.support;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.User;
import roomescape.theme.application.dto.ThemeCreateCommand;

@TestComponent
public class TestDataHelper {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert themeInsert;
    private final SimpleJdbcInsert reservationTimeInsert;

    public TestDataHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.reservationTimeInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public Long insertTheme(String name, String description, String thumbnailImgUrl) {
        return themeInsert.executeAndReturnKey(Map.of(
                "name", name,
                "description", description,
                "thumbnail_img_url", thumbnailImgUrl
        )).longValue();
    }

    public Long insertTheme(ThemeCreateCommand command) {
        return insertTheme(command.name(), command.description(), command.thumbnailImgUrl());
    }

    public Long insertReservationTime(LocalTime startAt) {
        return reservationTimeInsert.executeAndReturnKey(Map.of(
                "start_at", startAt
        )).longValue();
    }

    public Long insertReservation(String name, LocalDate date, Long themeId, Long timeId) {
        SimpleJdbcInsert reservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
        return reservationInsert.executeAndReturnKey(Map.of(
                "name", name,
                "date", date,
                "theme_id", themeId,
                "time_id", timeId,
                "status", ReservationStatus.PAYMENT_PENDING.name()
        )).longValue();
    }

    public Long insertWaiting(String name, LocalDate date, Long themeId, Long timeId) {
        SimpleJdbcInsert waitingInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
        return waitingInsert.executeAndReturnKey(Map.of(
                "name", name,
                "date", date,
                "theme_id", themeId,
                "time_id", timeId,
                "rank", nextWaitingRank(date, themeId, timeId)
        )).longValue();
    }

    private Integer nextWaitingRank(LocalDate date, Long themeId, Long timeId) {
        return jdbcTemplate.queryForObject("""
                        SELECT COALESCE(MAX(rank), 0) + 1
                        FROM waiting
                        WHERE date = ?
                          AND theme_id = ?
                          AND time_id = ?
                        """,
                Integer.class,
                date,
                themeId,
                timeId);
    }

    public Integer findWaitingRank(String name, ReservationSlot slot) {
        return jdbcTemplate.queryForObject("""
                        SELECT rank
                        FROM waiting
                        WHERE name = ?
                          AND date = ?
                          AND theme_id = ?
                          AND time_id = ?
                        """,
                Integer.class,
                name,
                slot.date(),
                slot.themeId(),
                slot.timeId());
    }

    public Reservation findReservationBySlot(ReservationSlot slot) {
        return jdbcTemplate.queryForObject("""
                        SELECT r.id, r.name, r.date, r.theme_id, r.time_id, rt.start_at, r.status
                        FROM reservation r
                        JOIN reservation_time rt ON r.time_id = rt.id
                        WHERE r.date = ?
                          AND r.theme_id = ?
                          AND r.time_id = ?
                        """,
                (rs, rowNum) -> Reservation.builder()
                        .id(rs.getLong("id"))
                        .user(User.builder()
                                .name(rs.getString("name"))
                                .build())
                        .slot(ReservationSlot.builder()
                                .date(rs.getDate("date").toLocalDate())
                                .themeId(rs.getLong("theme_id"))
                                .timeId(rs.getLong("time_id"))
                                .startAt(rs.getObject("start_at", LocalTime.class))
                                .build())
                        .status(ReservationStatus.valueOf(rs.getString("status")))
                        .build(),
                slot.date(),
                slot.themeId(),
                slot.timeId());
    }
}
