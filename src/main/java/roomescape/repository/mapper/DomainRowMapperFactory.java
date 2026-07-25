package roomescape.repository.mapper;

import org.springframework.stereotype.Component;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DomainRowMapperFactory {

    public TimeSlot mapTimeSlot(ResultSet rs) throws SQLException {
        return new TimeSlot(rs.getLong("t_id"), rs.getObject("start_at", LocalTime.class));
    }

    public Theme mapTheme(ResultSet rs) throws SQLException {
        return new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"), rs.getString("theme_thumbnail_url"));
    }

    public Session mapSession(ResultSet rs) throws SQLException {
        return new Session(rs.getLong("session_id"), rs.getObject("date", LocalDate.class), mapTimeSlot(rs), mapTheme(rs));
    }
}
