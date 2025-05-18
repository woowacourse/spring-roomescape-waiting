package roomescape;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;

@TestComponent
public class TestRepositoryHelper {

    @Autowired(required = false)
    private EntityManager em;

    public User saveUser(final User user) {
        em.persist(user);
        return em.find(User.class, user.id());
    }

    public User saveAnyUser() {
        return saveUser(TestFixtures.anyUser());
    }

    public TimeSlot saveTimeSlot(final TimeSlot timeSlot) {
        em.persist(timeSlot);
        return em.find(TimeSlot.class, timeSlot.id());
    }

    public TimeSlot saveAnyTimeSlot() {
        return saveTimeSlot(TestFixtures.anyTimeSlot());
    }

    public Theme saveTheme(final Theme theme) {
        em.persist(theme);
        return em.find(Theme.class, theme.id());
    }

    public Theme saveAnyTheme() {
        return saveTheme(TestFixtures.anyTheme());
    }

    public Reservation saveReservation(final Reservation reservation) {
        em.persist(reservation);
        return em.find(Reservation.class, reservation.id());
    }
}
