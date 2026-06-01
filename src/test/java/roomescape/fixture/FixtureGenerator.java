package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.SlotDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public class FixtureGenerator {

    private final ThemeDao themeDao;
    private final ReservationTimeDao reservationTimeDao;
    private final SlotDao slotDao;
    private final ReservationDao reservationDao;
    private final WaitingDao waitingDao;

    public FixtureGenerator(
            ThemeDao themeDao,
            ReservationTimeDao reservationTimeDao,
            SlotDao slotDao,
            ReservationDao reservationDao,
            WaitingDao waitingDao
    ) {
        this.themeDao = themeDao;
        this.reservationTimeDao = reservationTimeDao;
        this.slotDao = slotDao;
        this.reservationDao = reservationDao;
        this.waitingDao = waitingDao;
    }

    public Theme saveTheme(String name, String description, String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);
        return themeDao.save(theme);
    }

    public ReservationTime saveReservationTime(LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        return reservationTimeDao.save(reservationTime);
    }

    public Slot saveSlot(LocalDate date, ReservationTime time, Theme theme) {
        Slot slot = new Slot(date, time, theme);
        return slotDao.save(slot);
    }

    public Reservation saveReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        Slot savedSlot = saveSlot(date, time, theme);
        Reservation reservation = new Reservation(savedSlot, name);
        return reservationDao.save(reservation);
    }

    public Waiting saveWaiting(String name, LocalDate date, ReservationTime time, Theme theme, LocalDateTime createdAt) {
        Slot savedSlot = saveSlot(date, time, theme);
        Waiting waiting = new Waiting(createdAt, savedSlot.getId(), name);
        return waitingDao.save(waiting);
    }
}
