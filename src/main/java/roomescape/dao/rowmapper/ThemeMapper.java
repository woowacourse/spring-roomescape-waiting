package roomescape.dao.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.reservation.theme.Description;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.theme.ThemeName;
import roomescape.domain.reservation.theme.ThumbnailUrl;

public final class ThemeMapper {

    public static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> {
        return new Theme(
                rs.getLong("id"),
                ThemeName.parse(rs.getString("name")),
                Description.parse(rs.getString("description")),
                ThumbnailUrl.parse(rs.getString("url"))
        );
    };

    private ThemeMapper() {
    }
}
