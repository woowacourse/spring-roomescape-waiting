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
        String name = generateRandomAlphabetString(5);
        String email = name + "@email.com";
        User user = User.create(name, email, "password123");
        return userDao.save(user).getId().value();
    }

    public String insertUser(final String name, final String email) {
        User user = User.create(name, email, "password123");
        return userDao.save(user).getId().value();
    }

    public String insertReservation(final String slotId, final String userId) {
        User user = userDao.findById(Id.create(userId)).orElseThrow();
        ReservationSlot slot = slotDao.findById(Id.create(slotId)).orElseThrow();
        Reservation reservation = Reservation.create(user, slot);
        return reservationDao.save(reservation).getId().value();
    }

    public String insertSlot(final LocalDate date, final String timeId, final String themeId) {
        ReservationTime time = timeDao.findById(Id.create(timeId)).orElseThrow();
        Theme theme = themeDao.findById(Id.create(themeId)).orElseThrow();
        ReservationSlot slot = ReservationSlot.create(time, date, theme);
        return slotDao.save(slot).getId().value();
    }

    public String insertReservationTime() {
        LocalTime time = LocalTime.of(10, 0).plusSeconds((long) (Math.random() * 60));
        ReservationTime reservationTime = ReservationTime.create(time);
        return timeDao.save(reservationTime).getId().value();
    }

    public String insertReservationTime(final LocalTime time) {
        ReservationTime reservationTime = ReservationTime.create(time);
        return timeDao.save(reservationTime).getId().value();
    }

    public String insertTheme() {
        String name = generateRandomAlphabetString(5);
        Theme theme = Theme.create(name, "", "");
        return themeDao.save(theme).getId().value();
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
        reservationDao.deleteAllInBatch();
        slotDao.deleteAllInBatch();
        timeDao.deleteAllInBatch();
        themeDao.deleteAllInBatch();
        userDao.deleteAllInBatch();
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
