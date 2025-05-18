package roomescape;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.Email;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;
import roomescape.domain.user.UserRole;

public class TestFixtures {

    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    public static User user(final String name) {
        return new User(new UserName(name), new Email("email@email.com"), new Password("password"));
    }

    public static User anyUser() {
        return new User(new UserName("name"), new Email("email@email.com"), new Password("password"));
    }

    public static User anyUserWithId() {
        var user = anyUser();
        return new User(ID_GENERATOR.incrementAndGet(), user.name(), user.role(), user.email(), user.password());
    }

    public static User anyAdminWithId() {
        return new User(ID_GENERATOR.incrementAndGet(), new UserName("어드민"), UserRole.ADMIN, new Email("admin@email.com"), new Password("pw"));
    }

    public static TimeSlot timeSlot(final LocalTime startAt) {
        return new TimeSlot(startAt);
    }

    public static TimeSlot anyTimeSlot() {
        return new TimeSlot(LocalTime.of(10, 0));
    }

    public static TimeSlot anyTimeSlotWithId() {
        return new TimeSlot(ID_GENERATOR.incrementAndGet(), anyTimeSlot().startAt());
    }

    public static Theme theme(final String name) {
        return new Theme(name, "description", "thumbnail.jpg");
    }

    public static Theme anyTheme() {
        return new Theme("name", "description", "thumbnail.jpg");
    }

    public static Theme anyThemeWithId() {
        var theme = anyTheme();
        return new Theme(ID_GENERATOR.incrementAndGet(), theme.name(), theme.description(), theme.thumbnail());
    }

    public static Reservation anyReservationWithId() {
        return new Reservation(
            ID_GENERATOR.incrementAndGet(),
            anyUserWithId(),
            ReservationDateTime.forReserve(LocalDate.of(3000, 10, 1), anyTimeSlotWithId()),
            anyThemeWithId(),
            ReservationStatus.RESERVED
        );
    }
}
