package roomescape.test_util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.jpa.JpaReservationSlots;
import roomescape.infrastructure.jpa.JpaReservationTimes;
import roomescape.infrastructure.jpa.JpaReservations;
import roomescape.infrastructure.jpa.JpaThemes;
import roomescape.infrastructure.jpa.JpaUsers;
import roomescape.infrastructure.jpa.dao.JpaReservationDao;
import roomescape.infrastructure.jpa.dao.JpaReservationSlotDao;
import roomescape.infrastructure.jpa.dao.JpaReservationTimeDao;
import roomescape.infrastructure.jpa.dao.JpaThemeDao;
import roomescape.infrastructure.jpa.dao.JpaUserDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;
import java.util.UUID;

@TestComponent
@Import({JpaReservations.class, JpaReservationTimes.class, JpaThemes.class, JpaUsers.class, JpaReservationSlots.class})
public class JpaTestUtil {

    @Autowired
    private JpaReservationSlotDao slotDao;
    @Autowired
    private JpaReservationDao reservationDao;
    @Autowired
    private JpaReservationTimeDao timeDao;
    @Autowired
    private JpaUserDao userDao;
    @Autowired
    private JpaThemeDao themeDao;

    public String insertUser() {
        String id = generateId();
        String name = generateRandomAlphabetString(5);
        String email = name + "@email.com";
        userDao.save(User.restore(id, "USER", name, email, "password123"));
        return id;
    }

    public String insertUser(final String name, final String email) {
        String id = generateId();
        userDao.save(User.restore(id, "USER", name, email, "password123"));
        return id;
    }

    public String insertReservation(final String slotId, final String userId) {
        String id = generateId();
        User user = userDao.findById(Id.create(userId)).orElseThrow();
        ReservationSlot slot = slotDao.findById(Id.create(slotId)).orElseThrow();
        reservationDao.save(Reservation.restore(id, user, slot));
        return id;
    }

    public String insertSlot(final LocalDate date, final String timeId, final String themeId) {
        String id = generateId();
        ReservationTime time = timeDao.findById(Id.create(timeId)).orElseThrow();
        Theme theme = themeDao.findById(Id.create(themeId)).orElseThrow();
        slotDao.save(ReservationSlot.restore(id, time, date, theme));
        return id;
    }

    public String insertReservationTime() {
        String id = generateId();
        LocalTime time = LocalTime.of(10, 0).plusSeconds((long) (Math.random() * 60));
        timeDao.save(ReservationTime.restore(id, time));
        return id;
    }

    public String insertReservationTime(final LocalTime time) {
        String id = generateId();
        timeDao.save(ReservationTime.restore(id, time));
        return id;
    }

    public String insertTheme() {
        String id = generateId();
        String name = generateRandomAlphabetString(5);
        themeDao.save(Theme.restore(id, name, "", ""));
        return id;
    }

    public int countReservation() {
        return (int) reservationDao.count();
    }

    public int countReservationTime() {
        return (int) timeDao.count();
    }

    public int countTheme() {
        return (int) themeDao.count();
    }

    public void deleteAll() {
        reservationDao.deleteAll();
        slotDao.deleteAll();
        timeDao.deleteAll();
        themeDao.deleteAll();
        userDao.deleteAll();
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    public String generateRandomAlphabetString(int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(alphabet.length());
            result.append(alphabet.charAt(index));
        }

        return result.toString();
    }

}
