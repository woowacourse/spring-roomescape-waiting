package roomescape.persistence.jdbc;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.controller.admin.api.dto.response.AdminReservationSlotResponse;
import roomescape.controller.admin.api.query.AdminReservationQuery;
import roomescape.persistence.jdbc.mapper.AdminReservationSlotResponseRowMapper;

@Component
@RequiredArgsConstructor
public class JdbcAdminReservationQuery implements AdminReservationQuery {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<AdminReservationSlotResponse> getAllReservations() {
        String sql = """
                SELECT
                    r.id AS slot_id,
                    r.date AS reservation_date,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_image_url AS theme_thumbnail_image_url,
                    t.price AS theme_price,
                    t.is_active AS theme_is_active,
                    rt.id AS time_id,
                    rt.start_at AS time_start_at,
                    rt.status AS time_status,
                    re.id AS reservation_id,
                    re.name AS reservation_name,
                    re.status AS reservation_status,
                    re.created_at AS reservation_created_at
                FROM reservation_slot r
                JOIN theme t ON r.theme_id = t.id
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN reservation re ON re.slot_id = r.id
                WHERE re.status = 'RESERVED'
                  AND re.active_status = 'ACTIVE'
                ORDER BY r.date DESC, rt.start_at DESC, re.id DESC
                """;
        return jdbcTemplate.query(sql, AdminReservationSlotResponseRowMapper.ADMIN_RESERVATION_SLOT_RESPONSE_ROW_MAPPER);
    }
}
