package roomescape.controller.admin.api.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.controller.admin.api.dto.response.AdminThemeResponse;

@Component
@RequiredArgsConstructor
public class AdminThemeQuery {

    private static final RowMapper<AdminThemeResponse> ADMIN_THEME_RESPONSE_MAPPER = (rs, rowNum) ->
            new AdminThemeResponse(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail_image_url"),
                    rs.getBoolean("is_active")
            );

    private final JdbcTemplate jdbcTemplate;

    public List<AdminThemeResponse> getAllThemes() {
        String sql = "SELECT id, name, description, thumbnail_image_url, is_active FROM theme ORDER BY id ASC";
        return jdbcTemplate.query(sql, ADMIN_THEME_RESPONSE_MAPPER);
    }
}
