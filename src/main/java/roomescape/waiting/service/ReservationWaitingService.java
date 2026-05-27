package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingDao reservationWaitingDao;

    public ReservationWaitingService(ReservationWaitingDao reservationWaitingDao) {
        this.reservationWaitingDao = reservationWaitingDao;
    }

    public ReservationWaiting add(String name, Long themeId, LocalDate date, Long timeId) {
        validateDuplicatedWaiting(name, themeId, date, timeId);

        Long nextWaitingNumber = reservationWaitingDao.findNextWaitingNumber(themeId, date, timeId);
        ReservationWaiting reservationWaiting = new ReservationWaiting(name, themeId, date, timeId, nextWaitingNumber);

        return reservationWaitingDao.insert(reservationWaiting);
    }

    private void validateDuplicatedWaiting(String name, Long themeId, LocalDate date, Long timeId) {
        if (reservationWaitingDao.existsByNameAndDateAndThemeIdAndTimeId(name, themeId, date, timeId)) {
            throw new RoomescapeException(ErrorCode.DUPLICATED_RESERVATION_WAITING);
        }
    }
}
