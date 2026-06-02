package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.reservation.MyReservation;
import roomescape.reservation.Reservation;
import roomescape.reservation.dao.ReservationDao;
import roomescape.theme.dao.ThemeDao;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;
    private final TimeDao timeDao;

    public ReservationService(ReservationDao reservationDao, ThemeDao themeDao, TimeDao timeDao) {
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
        this.timeDao = timeDao;
    }

    public List<Reservation> findAll() {
        return reservationDao.selectAll();
    }

    public Reservation findById(Long id) {
        return reservationDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public List<MyReservation> findAllByName(String name) {
        return reservationDao.selectAllCombinedByName(name);
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
