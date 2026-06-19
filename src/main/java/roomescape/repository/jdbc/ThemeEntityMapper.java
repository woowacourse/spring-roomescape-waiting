package roomescape.repository.jdbc;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.Theme;

public final class ThemeEntityMapper {

    public static final RowMapper<Theme> THEME_MAPPER = (rs, rowNum) -> Theme.restore(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail_image_url"),
            rs.getLong("price"),
            rs.getBoolean("is_active")
    );

    private ThemeEntityMapper() {
    }
}
