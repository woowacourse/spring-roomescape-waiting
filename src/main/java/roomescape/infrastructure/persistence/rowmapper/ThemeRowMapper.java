package roomescape.infrastructure.persistence.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import roomescape.domain.Theme;
import roomescape.domain.ThemeName;

public class ThemeRowMapper {

    private ThemeRowMapper() {
    }

    public static Theme mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Theme(
                rs.getLong("theme_id"),
                new ThemeName(rs.getString("theme_name")),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );
    }
}
