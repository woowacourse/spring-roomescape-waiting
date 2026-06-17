package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.Theme;

public final class ThemeRowMapper {

    public static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail_image_url"),
            rs.getInt("price"),
            rs.getBoolean("is_active")
    );

    private ThemeRowMapper() {
    }
}
