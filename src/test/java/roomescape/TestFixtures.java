package roomescape;

import java.time.LocalTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.Email;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;

public class TestFixtures {

    public static User createUser(final String name) {
        return new User(new UserName(name), new Email("email@email.com"), new Password("password"));
    }

    public static User anyUser() {
        return new User(new UserName("name"), new Email("email@email.com"), new Password("password"));
    }

    public static TimeSlot createTimeSlot(final LocalTime startAt) {
        return new TimeSlot(startAt);
    }

    public static TimeSlot anyTimeSlot() {
        return new TimeSlot(LocalTime.of(10, 0));
    }

    public static Theme createTheme(final String name) {
        return new Theme(name, "description", "thumbnail.jpg");
    }

    public static Theme anyTheme() {
        return new Theme("name", "description", "thumbnail.jpg");
    }
}
