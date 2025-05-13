package roomescape.infrastructure;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.User;
import roomescape.domain.UserRole;

public final class RowMappers {

    public static final RowMapper<User> USER =
        (rs, rowNum) -> {
            var id = rs.getLong("id");
            var name = rs.getString("name");
            var role = UserRole.valueOf(rs.getString("role"));
            var email = rs.getString("email");
            var password = rs.getString("password");
            return new User(id, name, role, email, password);
        };

    public static final RowMapper<Reservation> RESERVATION =
        (rs, rowNum) -> {
            var id = rs.getLong("id");
            var userId = rs.getLong("user_id");
            var userName = rs.getString("user_name");
            var userRole = UserRole.valueOf(rs.getString("user_role"));
            var userEmail = rs.getString("user_email");
            var userPassword = rs.getString("user_password");
            var date = rs.getDate("date").toLocalDate();
            var timeSlotId = rs.getLong("time_id");
            var time = rs.getTime("start_at").toLocalTime();
            var themeId = rs.getLong("theme_id");
            var themeName = rs.getString("theme_name");
            var themeDescription = rs.getString("theme_description");
            var themeThumbnail = rs.getString("theme_thumbnail");

            var timeSlot = new TimeSlot(timeSlotId, time);
            var theme = new Theme(themeId, themeName, themeDescription, themeThumbnail);
            var user = new User(userId, userName, userRole, userEmail, userPassword);
            return Reservation.ofExisting(id, user, date, timeSlot, theme);
        };

    public static final RowMapper<Theme> THEME =
        (rs, rowNum) -> {
            var id = rs.getLong("id");
            var name = rs.getString("name");
            var description = rs.getString("description");
            var thumbnail = rs.getString("thumbnail");
            return new Theme(id, name, description, thumbnail);
        };

    public static final RowMapper<TimeSlot> TIME_SLOT = (rs, rowNum) -> {
        var id = rs.getLong("id");
        var startAt = rs.getTime("start_at").toLocalTime();
        return new TimeSlot(id, startAt);
    };

    private RowMappers() {
    }
}
