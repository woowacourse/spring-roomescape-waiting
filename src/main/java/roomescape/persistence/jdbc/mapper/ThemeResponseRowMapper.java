package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.controller.client.api.dto.response.ThemeResponse;

public final class ThemeResponseRowMapper {

    public static final RowMapper<ThemeResponse> THEME_RESPONSE_ROW_MAPPER = (rs, rowNum) ->
            new ThemeResponse(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail_image_url"),
                    rs.getInt("price")
            );

    private ThemeResponseRowMapper() {
    }
}
