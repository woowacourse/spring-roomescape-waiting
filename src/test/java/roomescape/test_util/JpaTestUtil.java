package roomescape.test_util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.JpaReservationDao;
import roomescape.infrastructure.JpaReservationTimeDao;
import roomescape.infrastructure.JpaReservationTimes;
import roomescape.infrastructure.JpaReservations;
import roomescape.infrastructure.JpaThemeDao;
import roomescape.infrastructure.JpaThemes;
import roomescape.infrastructure.JpaUserDao;
import roomescape.infrastructure.JpaUsers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;
import java.util.UUID;

@TestComponent
@Import({JpaReservations.class, JpaReservationTimes.class, JpaThemes.class, JpaUsers.class})
public class JpaTestUtil {

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

    public String insertReservation(final LocalDate date, final String timeId, final String themeId, final String userId) {
        String id = generateId();
        User user = userDao.findById(Id.create(userId)).orElseThrow();
        ReservationTime time = timeDao.findById(Id.create(timeId)).orElseThrow();
        Theme theme = themeDao.findById(Id.create(themeId)).orElseThrow();
        reservationDao.save(Reservation.restore(id, user, date, time, theme));
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
