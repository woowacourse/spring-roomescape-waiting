package roomescape.controller.client.api.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.controller.client.api.dto.response.ThemeTimesResponse;
import roomescape.repository.dto.TimeSlotProjection;

@Component
@RequiredArgsConstructor
public class ThemeTimesQuery {

    private final JdbcTemplate jdbcTemplate;

    public List<ThemeTimesResponse> getThemeReservationStatus(long themeId, LocalDate date) {
        String sql = """
                SELECT
                    rt.id AS time_id,
                    rt.start_at AS time_start_at,
                    NOT EXISTS (
                        SELECT 1
                        FROM reservation r
                        JOIN reservation_entry re ON re.reservation_id = r.id
                        WHERE r.time_id = rt.id
                          AND r.theme_id = ?
                          AND r.date = ?
                          AND re.status = 'RESERVED'
                    ) AS is_reservable
                FROM reservation_time rt
                WHERE rt.status = 'ACTIVE'
                ORDER BY rt.start_at ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new TimeSlotProjection(
                        rs.getLong("time_id"),
                        rs.getTime("time_start_at").toLocalTime(),
                        rs.getBoolean("is_reservable")
                ), themeId, date)
                .stream()
                .map(projection -> toResponse(projection, date))
                .toList();
    }

    private ThemeTimesResponse toResponse(TimeSlotProjection projection, LocalDate date) {
        if (LocalDateTime.of(date, projection.startAt()).isBefore(LocalDateTime.now())) {
            return new ThemeTimesResponse(projection.id(), projection.startAt(), false, "UNAVAILABLE");
        }

        String status = projection.isReservable() ? "RESERVABLE" : "WAITING_AVAILABLE";
        return new ThemeTimesResponse(projection.id(), projection.startAt(), projection.isReservable(), status);
    }
}
