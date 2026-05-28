package roomescape.controller.admin.api.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.controller.admin.api.dto.response.AdminReservationEntryResponse;
import roomescape.controller.admin.api.dto.response.AdminReservationResponse;
import roomescape.controller.admin.api.dto.response.AdminReservationTimeResponse;
import roomescape.controller.admin.api.dto.response.AdminThemeResponse;

@Component
@RequiredArgsConstructor
public class AdminReservationQuery {

    private static final RowMapper<AdminReservationResponse> ADMIN_RESERVATION_RESPONSE_MAPPER = (rs, rowNum) ->
            new AdminReservationResponse(
                    rs.getLong("reservation_id"),
                    rs.getDate("reservation_date").toLocalDate(),
                    new AdminThemeResponse(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("theme_description"),
                            rs.getString("theme_thumbnail_image_url"),
                            rs.getBoolean("theme_is_active")
                    ),
                    new AdminReservationTimeResponse(
                            rs.getLong("time_id"),
                            rs.getTime("time_start_at").toLocalTime(),
                            rs.getString("time_status")
                    ),
                    new AdminReservationEntryResponse(
                            rs.getLong("entry_id"),
                            rs.getString("entry_name"),
                            rs.getString("entry_status"),
                            rs.getTimestamp("entry_created_at").toLocalDateTime()
                    )
            );

    private final JdbcTemplate jdbcTemplate;

    public List<AdminReservationResponse> getAllReservations() {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.date AS reservation_date,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_image_url AS theme_thumbnail_image_url,
                    t.is_active AS theme_is_active,
                    rt.id AS time_id,
                    rt.start_at AS time_start_at,
                    rt.status AS time_status,
                    re.id AS entry_id,
                    re.name AS entry_name,
                    re.status AS entry_status,
                    re.created_at AS entry_created_at
                FROM reservation r
                JOIN theme t ON r.theme_id = t.id
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN reservation_entry re ON re.reservation_id = r.id
                WHERE re.status = 'RESERVED'
                ORDER BY r.date DESC, rt.start_at DESC, re.id DESC
                """;
        return jdbcTemplate.query(sql, ADMIN_RESERVATION_RESPONSE_MAPPER);
    }
}
