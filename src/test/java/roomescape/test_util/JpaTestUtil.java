package roomescape.test_util;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.Status;
import roomescape.infrastructure.JpaReservationDao;
import roomescape.infrastructure.JpaReservationTimeDao;
import roomescape.infrastructure.JpaThemeDao;
import roomescape.infrastructure.JpaUserDao;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class JpaTestUtil {

    private final JpaReservationDao reservationDao;
    private final JpaReservationTimeDao timeDao;
    private final JpaUserDao userDao;
    private final JpaThemeDao themeDao;

    @Autowired
    public JpaTestUtil(JpaReservationDao reservationDao, JpaReservationTimeDao timeDao,
                       JpaUserDao userDao, JpaThemeDao themeDao) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.userDao = userDao;
        this.themeDao = themeDao;
    }

    public void insertUser(final String id, final String name) {
        userDao.save(User.restore(id, "USER", name, name + "@email.com", "password123"));
    }

    public void insertReservation(final String id, final LocalDate date, final String timeId, final String themeId,
                                  final String userId) {
        User user = userDao.findById(Id.create(userId)).get();
        ReservationTime time = timeDao.findById(Id.create(timeId)).get();
        Theme theme = themeDao.findById(Id.create(themeId)).get();
        reservationDao.save(Reservation.restore(id, user, date, time, theme, Status.RESERVED, LocalDateTime.now()));
    }

    public void insertReservationTime(final String id, final LocalTime time) {
        timeDao.save(ReservationTime.restore(id, time));
    }

    public void insertTheme(final String id, final String name) {
        themeDao.save(Theme.restore(id, name, "", ""));
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
}
