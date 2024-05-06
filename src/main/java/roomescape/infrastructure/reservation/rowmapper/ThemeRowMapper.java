package roomescape.infrastructure.reservation.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeName;

public class ThemeRowMapper {

    private ThemeRowMapper() {
    }

    public static Theme mapRow(ResultSet rs) throws SQLException {
        return new Theme(
                rs.getLong("id"),
                new ThemeName(rs.getString("name")),
                rs.getString("description"),
                rs.getString("thumbnail")
        );
    }

    public static Theme joinedMapRow(ResultSet rs) throws SQLException {
        return new Theme(
                rs.getLong("theme_id"),
                new ThemeName(rs.getString("theme_name")),
                rs.getString("description"),
                rs.getString("thumbnail")
        );
    }
}
