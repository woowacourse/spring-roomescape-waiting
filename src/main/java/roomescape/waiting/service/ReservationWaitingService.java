package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.theme.dao.ThemeDao;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;

@Service
public class ReservationWaitingService {

    private final ReservationDao reservationDao;
    private final ReservationWaitingDao reservationWaitingDao;
    private final ThemeDao themeDao;
    private final TimeDao timeDao;

    public ReservationWaitingService(ReservationDao reservationDao, ReservationWaitingDao reservationWaitingDao,
                                     ThemeDao themeDao, TimeDao timeDao) {
        this.reservationDao = reservationDao;
        this.reservationWaitingDao = reservationWaitingDao;
        this.themeDao = themeDao;
        this.timeDao = timeDao;
    }

    public ReservationWaiting findById(Long id) {
        return reservationWaitingDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_WAITING_NOT_FOUND));
    }

    @Transactional
    public ReservationWaiting add(String name, Long themeId, LocalDate date, Long timeId) {
        validateThemeExists(themeId);
        ReservationTime reservationTime = timeDao.selectById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));
        validateDateTime(date, reservationTime);

        validateNotExistsReservation(themeId, date, timeId);
        validateDuplicatedReservation(name, themeId, date, timeId);
        validateDuplicatedWaiting(name, themeId, date, timeId);


        Long nextWaitingNumber = reservationWaitingDao.findNextWaitingNumber(themeId, date, timeId);
        ReservationWaiting reservationWaiting = new ReservationWaiting(name, themeId, date, reservationTime, nextWaitingNumber);

        return reservationWaitingDao.insert(reservationWaiting);
    }

    @Transactional
    public void deleteByIdIfNameMatches(Long id, String name) {
        ReservationWaiting originReservationWaiting = reservationWaitingDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_WAITING_NOT_FOUND));

        originReservationWaiting.validateSameName(name);
        validateDateTime(originReservationWaiting.getDate(), originReservationWaiting.getTime());
        reservationWaitingDao.deleteById(id);
    }

    private void validateNotExistsReservation(Long themeId, LocalDate date, Long timeId) {
        if (reservationDao.notExistsByDateAndThemeIdAndTimeId(themeId, date, timeId)) {
            throw new RoomescapeException(ErrorCode.RESERVATION_NOT_EXISTS);
        }
    }

    private void validateDuplicatedReservation(String name, Long themeId, LocalDate date, Long timeId) {
        if (reservationDao.existsByNameAndDateAndThemeIdAndTimeId(name, themeId, date, timeId)) {
            throw new RoomescapeException(ErrorCode.DUPLICATED_RESERVATION);
        }
    }

    private void validateDuplicatedWaiting(String name, Long themeId, LocalDate date, Long timeId) {
        if (reservationWaitingDao.existsByNameAndDateAndThemeIdAndTimeId(name, themeId, date, timeId)) {
            throw new RoomescapeException(ErrorCode.DUPLICATED_RESERVATION_WAITING);
        }
    }

    private void validateThemeExists(Long themeId) {
        if (themeDao.selectById(themeId).isEmpty()) {
            throw new RoomescapeException(ErrorCode.THEME_NOT_FOUND);
        }
    }

    private void validateDateTime(LocalDate date, ReservationTime time) {
        if (time.isBeforeDateTime(date, time)) {
            throw new RoomescapeException(ErrorCode.CANNOT_CANCEL_PAST_RESERVATION_WAITING);
        }
    }
}
