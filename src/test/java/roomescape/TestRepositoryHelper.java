package roomescape;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;

@TestComponent
@Transactional
public class TestRepositoryHelper {

    @Autowired
    private EntityManager em;

    private List<String> tableNames;

    @PostConstruct
    public void init() {
        tableNames = lookUpTableNames();
    }

    public void clearDatabases() {
        flushAndClear();
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        tableNames.forEach(tableName -> em.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate());
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    public void flushAndClear() {
        em.flush();
        em.clear();
    }

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

    private List<String> lookUpTableNames() {
        return em.getMetamodel().getEntities().stream()
            .filter(e -> e.getJavaType().getAnnotation(Entity.class) != null)
            .map(e -> e.getName().toLowerCase())
            .toList();
    }
}
