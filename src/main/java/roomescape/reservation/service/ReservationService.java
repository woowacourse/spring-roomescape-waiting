package roomescape.reservation.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.reservation.Reservation;
import roomescape.reservation.dao.ReservationDao;
import roomescape.theme.dao.ThemeDao;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;
    private final TimeDao timeDao;
    private final ReservationWaitingDao reservationWaitingDao;

    public ReservationService(ReservationDao reservationDao, ThemeDao themeDao, TimeDao timeDao, ReservationWaitingDao reservationWaitingDao) {
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
        this.timeDao = timeDao;
        this.reservationWaitingDao = reservationWaitingDao;
    }

    public List<Reservation> findAll() {
        return reservationDao.selectAll();
    }

    public Reservation findById(Long id) {
        return reservationDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Transactional
    public List<Reservation> findAllByName(String name) {
        return reservationDao.selectByName(name);
    }

    @Transactional
    public Reservation add(String name, Long themeId, LocalDate date, Long timeId) {
        ReservationTime time = timeDao.selectById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));
        validateDateTime(date, time, ErrorCode.PAST_RESERVATION);
        validateThemeExists(themeId);

        if (reservationDao.existsByThemeIdAndDateAndTimeId(themeId, date, timeId)) {
            throw new RoomescapeException(ErrorCode.RESERVATION_ALREADY_EXISTS);
        }

        Reservation newReservation = new Reservation(name, themeId, date, time);
        try {
            return reservationDao.insert(newReservation);
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(ErrorCode.RESERVATION_ALREADY_EXISTS);
        }
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

        if (reservationDao.existsByThemeIdAndDateAndTimeId(themeId, date, timeId)) {
            throw new RoomescapeException(ErrorCode.RESERVATION_ALREADY_EXISTS);
        }

        return reservationDao.updateDateTimeById(id, date, timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Transactional
    public void deleteById(Long id) {
        Reservation originReservation = reservationDao.selectByIdForUpdate(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));

        cancelReservation(id, originReservation);
    }

    @Transactional
    public void deleteByIdIfNameMatches(Long id, String name) {
        Reservation originReservation = reservationDao.selectByIdForUpdate(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));

        originReservation.validateSameName(name, ErrorCode.CANNOT_DELETE_OTHER_RESERVATION);
        validateDateTime(originReservation.getDate(), originReservation.getTime(),
                ErrorCode.CANNOT_DELETE_PAST_RESERVATION);

        cancelReservation(id, originReservation);
    }

    private void cancelReservation(Long id, Reservation originReservation) {
        Optional<ReservationWaiting> firstWaiting = reservationWaitingDao.selectFirstByThemeAndDateAndTimeForUpdate(
                originReservation.getThemeId(),
                originReservation.getDate(),
                originReservation.getTime());

        if (firstWaiting.isEmpty()) {
            reservationDao.deleteById(id);
            return;
        }

        ReservationWaiting reservationWaiting = firstWaiting.get();
        reservationWaitingDao.deleteById(reservationWaiting.getId());
        reservationDao.deleteById(id);
        reservationDao.insert(new Reservation(
                reservationWaiting.getName(),
                reservationWaiting.getThemeId(),
                reservationWaiting.getDate(),
                reservationWaiting.getTime()
        ));
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
