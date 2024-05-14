package roomescape.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Thumbnail;

@Component
public class ThemeRowMapper implements RowMapper<Theme> {

    @Override
    public Theme mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new Theme(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                new Thumbnail(rs.getString("thumbnail"))
        );
    }
}
