package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.controller.client.api.dto.response.ReservationResponse;
import roomescape.controller.client.api.dto.response.ReservationSearchResponse;
import roomescape.controller.client.api.dto.response.ReservationDetailResponse;
import roomescape.controller.client.api.dto.response.ReservationTimeResponse;
import roomescape.controller.client.api.dto.response.ThemeResponse;

public final class ReservationQueryRowMapper {

    public static final RowMapper<ReservationSearchResponse> RESERVATION_SEARCH_ROW_MAPPER = (rs, rowNum) ->
            new ReservationSearchResponse(
                    rs.getLong("res_id"),
                    rs.getString("res_name"),
                    rs.getDate("res_date").toLocalDate(),
                    rs.getTime("res_start_at").toLocalTime(),
                    rs.getString("theme_name"),
                    rs.getString("res_status"),
                    rs.getObject("waiting_rank", Integer.class),
                    rs.getString("order_id"),
                    rs.getString("order_status"),
                    rs.getString("payment_key"),
                    rs.getObject("payment_amount", Long.class),
                    rs.getString("payment_status")
            );

    public static final RowMapper<ReservationDetailResponse> RESERVATION_DETAIL_ROW_MAPPER = (rs, rowNum) ->
            new ReservationDetailResponse(
                    rs.getLong("slot_id"),
                    rs.getDate("slot_date").toLocalDate(),
                    new ThemeResponse(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("theme_description"),
                            rs.getString("theme_thumbnail_image_url"),
                            rs.getInt("theme_price")
                    ),
                    new ReservationTimeResponse(
                            rs.getLong("time_id"),
                            rs.getTime("time_start_at").toLocalTime()
                    ),
                    new ReservationResponse(
                            rs.getLong("reservation_id"),
                            rs.getString("reservation_name"),
                            rs.getString("reservation_status"),
                            rs.getTimestamp("reservation_created_at").toLocalDateTime()
                    )
            );

    private ReservationQueryRowMapper() {
    }
}
