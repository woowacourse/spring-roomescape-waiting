package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingDao reservationWaitingDao;
    private final TimeDao timeDao;

    public ReservationWaitingService(ReservationWaitingDao reservationWaitingDao, TimeDao timeDao) {
        this.reservationWaitingDao = reservationWaitingDao;
        this.timeDao = timeDao;
    }

    public ReservationWaiting add(String name, Long themeId, LocalDate date, Long timeId) {
        validateDuplicatedWaiting(name, themeId, date, timeId);

        ReservationTime reservationTime = timeDao.selectById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Long nextWaitingNumber = reservationWaitingDao.findNextWaitingNumber(themeId, date, timeId);
        ReservationWaiting reservationWaiting = new ReservationWaiting(name, themeId, date, reservationTime, nextWaitingNumber);

        return reservationWaitingDao.insert(reservationWaiting);
    }

    public void deleteByIdIfNameMatches(Long id, String name) {
        ReservationWaiting originReservationWaiting = reservationWaitingDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_WAITING_NOT_FOUND));

        originReservationWaiting.validateSameName(name);
        validateDateTime(originReservationWaiting.getDate(), originReservationWaiting.getReservationTime());
        reservationWaitingDao.deleteById(id);
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
