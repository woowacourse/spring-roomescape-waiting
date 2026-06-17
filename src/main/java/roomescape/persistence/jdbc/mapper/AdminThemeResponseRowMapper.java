package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.controller.admin.api.dto.response.AdminThemeResponse;

public final class AdminThemeResponseRowMapper {

    public static final RowMapper<AdminThemeResponse> ADMIN_THEME_RESPONSE_ROW_MAPPER = (rs, rowNum) ->
            new AdminThemeResponse(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail_image_url"),
                    rs.getInt("price"),
                    rs.getBoolean("is_active")
            );

    private AdminThemeResponseRowMapper() {
    }
}
