package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.reservation.MyReservation;
import roomescape.reservation.Reservation;
import roomescape.reservation.dao.ReservationDao;
import roomescape.theme.Theme;
import roomescape.theme.dao.ThemeDao;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

@Service
public class ReservationService {
    private static final String RESERVED_STATUS = "RESERVED";
    private static final String WAITING_STATUS = "WAITING";
    private static final String RESERVED_RESOURCE = "reservation";
    private static final String WAITING_RESOURCE = "waiting";
    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;
    private final TimeDao timeDao;
    private final ReservationWaitingDao waitingDao;

    public ReservationService(ReservationDao reservationDao, ThemeDao themeDao, TimeDao timeDao,
                              ReservationWaitingDao waitingDao) {
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
        this.timeDao = timeDao;
        this.waitingDao = waitingDao;
    }

    public List<Reservation> findAll() {
        return reservationDao.selectAll();
    }

    public Reservation findById(Long id) {
        return reservationDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<MyReservation> findAllByName(String name) {
        List<MyReservation> myReservations = new ArrayList<>();
        List<Reservation> reservations = reservationDao.selectByName(name);
        for (Reservation reservation : reservations) {
            Theme theme = themeDao.selectById(reservation.getThemeId())
                    .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_NOT_FOUND));
            myReservations.add(new MyReservation(reservation, theme, RESERVED_RESOURCE, RESERVED_STATUS));
        }

        List<ReservationWaiting> reservationWaitings = waitingDao.selectByName(name);
        for (ReservationWaiting reservationWaiting : reservationWaitings) {
            Theme theme = themeDao.selectById(reservationWaiting.getThemeId())
                    .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_NOT_FOUND));
            myReservations.add(new MyReservation(reservationWaiting, theme, WAITING_RESOURCE, WAITING_STATUS));
        }
        return myReservations;
    }

    @Transactional
    public Reservation add(String name, Long themeId, LocalDate date, Long timeId) {
        ReservationTime time = timeDao.selectById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));
        validateDateTime(date, time, ErrorCode.PAST_RESERVATION);
        validateThemeExists(themeId);

        List<Reservation> reservedList = reservationDao.selectByThemeIdAndDate(themeId, date);
        for (Reservation reserved : reservedList) {
            validateReserved(timeId, reserved.getTime());
        }

        Reservation newReservation = new Reservation(name, themeId, date, time);
        return reservationDao.insert(newReservation);
    }

    @Transactional
    public Reservation modifyDateTimeByName(Long id, String name, Long themeId, LocalDate date, Long timeId) {
        Reservation originReservation = reservationDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));

        originReservation.validateSameName(name, ErrorCode.CANNOT_MODIFY_OTHER_RESERVATION);

        ReservationTime time = timeDao.selectById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));
        validateDateTime(date, time, ErrorCode.PAST_RESERVATION);
        validateThemeExists(themeId);

        List<Reservation> reservedList = reservationDao.selectByThemeIdAndDate(themeId, date);
        for (Reservation reserved : reservedList) {
            validateReserved(timeId, reserved.getTime());
        }

        return reservationDao.updateDateTimeById(id, date, timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public void deleteById(Long id) {
        reservationDao.deleteById(id);
    }

    public void deleteByIdIfNameMatches(Long id, String name) {
        Reservation originReservation = reservationDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));

        originReservation.validateSameName(name, ErrorCode.CANNOT_DELETE_OTHER_RESERVATION);

        validateDateTime(originReservation.getDate(), originReservation.getTime(),
                ErrorCode.CANNOT_DELETE_PAST_RESERVATION);

        reservationDao.deleteById(id);
    }

    private void validateReserved(Long timeId, ReservationTime reservedTime) {
        if (timeId.equals(reservedTime.getId())) {
            throw new RoomescapeException(ErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    private void validateDateTime(LocalDate date, ReservationTime time, ErrorCode errorCode) {
        if (time.isBeforeDateTime(date, time)) {
            throw new RoomescapeException(errorCode);
        }
    }

    private void validateThemeExists(Long themeId) {
        themeDao.selectById(themeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_NOT_FOUND));
    }
}
