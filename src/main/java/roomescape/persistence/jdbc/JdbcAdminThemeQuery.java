package roomescape.persistence.jdbc;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.controller.admin.api.dto.response.AdminThemeResponse;
import roomescape.controller.admin.api.query.AdminThemeQuery;
import roomescape.persistence.jdbc.mapper.AdminThemeResponseRowMapper;

@Component
@RequiredArgsConstructor
public class JdbcAdminThemeQuery implements AdminThemeQuery {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<AdminThemeResponse> getAllThemes() {
        String sql = "SELECT id, name, description, thumbnail_image_url, price, is_active FROM theme ORDER BY id ASC";
        return jdbcTemplate.query(sql, AdminThemeResponseRowMapper.ADMIN_THEME_RESPONSE_ROW_MAPPER);
    }
}
