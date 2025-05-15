package roomescape.reservation.infrastructure.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static java.util.Map.entry;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookedThemeRowMapper implements RowMapper<Map.Entry<Theme, Integer>> {

    public static final BookedThemeRowMapper INSTANCE = new BookedThemeRowMapper();

    @Override
    public Map.Entry<Theme, Integer> mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        final Theme theme = Theme.withId(
                ThemeId.from(resultSet.getLong("id")),
                ThemeName.from(resultSet.getString("name")),
                ThemeDescription.from(resultSet.getString("description")),
                ThemeThumbnail.from(resultSet.getString("thumbnail"))
        );
        final int bookedCount = resultSet.getInt("booked_count");
        return entry(theme, bookedCount);
    }
}
