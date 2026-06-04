package roomescape.waiting.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingDao reservationWaitingDao;
    private final ReservationDao reservationDao;
    private final TimeDao timeDao;

    public ReservationWaitingService(ReservationWaitingDao reservationWaitingDao, ReservationDao reservationDao, TimeDao timeDao) {
        this.reservationWaitingDao = reservationWaitingDao;
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
    }

    public ReservationWaiting findById(Long id) {
        return reservationWaitingDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_WAITING_NOT_FOUND));
    }

    public List<ReservationWaiting> findByName(String name) {
        return reservationWaitingDao.selectByName(name);
    }

    @Transactional
    public ReservationWaiting add(String name, Long themeId, LocalDate date, Long timeId) {
        validateReservationExists(themeId, date, timeId);
        validateAlreadyReserved(name, themeId, date, timeId);
        validateDuplicatedWaiting(name, themeId, date, timeId);

        ReservationTime reservationTime = timeDao.selectById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        ReservationWaiting reservationWaiting = new ReservationWaiting(name, themeId, date, reservationTime);

        try {
            return reservationWaitingDao.insert(reservationWaiting);
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(ErrorCode.DUPLICATED_RESERVATION_WAITING);
        }
    }

    @Transactional
    public void deleteByIdIfNameMatches(Long id, String name) {
        ReservationWaiting originReservationWaiting = reservationWaitingDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_WAITING_NOT_FOUND));

        originReservationWaiting.validateSameName(name);
        validateDateTime(originReservationWaiting.getDate(), originReservationWaiting.getTime());
        reservationWaitingDao.deleteById(id);
    }

    private void validateReservationExists(Long themeId, LocalDate date, Long timeId) {
        if (!reservationDao.existsByThemeIdAndDateAndTimeIdForUpdate(themeId, date, timeId)) {
            throw new RoomescapeException(ErrorCode.CANNOT_WAIT_WITHOUT_RESERVATION);
        }
    }

    private void validateAlreadyReserved(String name, Long themeId, LocalDate date, Long timeId) {
        if (reservationDao.existsByNameAndThemeIdAndDateAndTimeId(name, themeId, date, timeId)) {
            throw new RoomescapeException(ErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    private void validateDuplicatedWaiting(String name, Long themeId, LocalDate date, Long timeId) {
        if (reservationWaitingDao.existsByNameAndDateAndThemeIdAndTimeId(name, themeId, date, timeId)) {
            throw new RoomescapeException(ErrorCode.DUPLICATED_RESERVATION_WAITING);
        }
    }

    private void validateDateTime(LocalDate date, ReservationTime time) {
        if (time.isBeforeDateTime(date, time)) {
            throw new RoomescapeException(ErrorCode.CANNOT_CANCEL_PAST_RESERVATION_WAITING);
        }
    }
}
