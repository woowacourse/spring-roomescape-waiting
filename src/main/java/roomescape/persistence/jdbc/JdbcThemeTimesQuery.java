package roomescape.persistence.jdbc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.controller.client.api.dto.response.ThemeTimesResponse;
import roomescape.controller.client.api.dto.response.TimeSlotProjection;
import roomescape.controller.client.api.query.ThemeTimesQuery;
import roomescape.persistence.jdbc.mapper.TimeSlotProjectionRowMapper;

@Component
@RequiredArgsConstructor
public class JdbcThemeTimesQuery implements ThemeTimesQuery {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ThemeTimesResponse> getThemeReservationStatus(long themeId, LocalDate date) {
        String sql = """
                SELECT
                    rt.id AS time_id,
                    rt.start_at AS time_start_at,
                    NOT EXISTS (
                        SELECT 1
                        FROM reservation_slot r
                        JOIN reservation re ON re.slot_id = r.id
                        WHERE r.time_id = rt.id
                          AND r.theme_id = ?
                          AND r.date = ?
                          AND re.status = 'RESERVED'
                    ) AS is_reservable
                FROM reservation_time rt
                WHERE rt.status = 'ACTIVE'
                ORDER BY rt.start_at ASC
                """;

        return jdbcTemplate.query(sql, TimeSlotProjectionRowMapper.TIME_SLOT_PROJECTION_ROW_MAPPER, themeId, date)
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
