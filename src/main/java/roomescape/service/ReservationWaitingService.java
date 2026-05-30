package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ReservationWaitingErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ReservationWaitingDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateReservationWaitingCommand;
import roomescape.dto.response.ReservationWaitingResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingDao reservationWaitingDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;

    public ReservationWaitingService(ReservationWaitingDao reservationWaitingDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao, ReservationDao reservationDao) {
        this.reservationWaitingDao = reservationWaitingDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    @Transactional
    public ReservationWaitingResponse addReservationWaiting(CreateReservationWaitingCommand command, LocalDateTime now) {
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());

        validateReservationExists(command.reservationDate(), command.timeId(), command.themeId());
        validateUniqueReservationWaiting(command.name(), command.reservationDate(), command.timeId(), command.themeId());
        validatePastDatetime(command.reservationDate(), now, reservationTime);

        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId(command.name(), now, command.reservationDate(), reservationTime, theme);
        ReservationWaiting savedReservationWaiting = reservationWaitingDao.insert(reservationWaiting);

        int order = reservationWaitingDao.countOrder(command.reservationDate(), command.timeId(), command.themeId(), savedReservationWaiting.getId());
        return ReservationWaitingResponse.from(savedReservationWaiting, order);
    }

    @Transactional
    public void delete(Long reservationWaitingId) {
        int deleted = reservationWaitingDao.delete(reservationWaitingId);
        if (deleted == 0) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.RESERVATION_WAITING_NOT_FOUND);
        }
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeDao.selectById(timeId)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.NOT_FOUND));
    }

    private Theme getTheme(long themeId) {
        return themeDao.selectById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.NOT_FOUND));
    }

    private void validateReservationExists(LocalDate date, long timeId, long themeId) {
        boolean exists = reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (!exists) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    private void validateUniqueReservationWaiting(String name, LocalDate reservationDate, long timeId, long themeId) {
        boolean exists = reservationWaitingDao.existsByNameAndDateAndTimeIdAndThemeId(name, reservationDate, timeId, themeId);
        if (exists) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.DUPLICATE);
        }
    }

    private void validatePastDatetime(LocalDate date, LocalDateTime now, ReservationTime reservationTime) {
        if (reservationTime.isPast(date, now)) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.PAST_DATETIME);
        }
    }
}
