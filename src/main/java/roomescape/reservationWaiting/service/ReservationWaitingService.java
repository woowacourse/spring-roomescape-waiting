package roomescape.reservationWaiting.service;

import org.springframework.stereotype.Service;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationErrorCode;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ReservationWaitingErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservationWaiting.dao.ReservationWaitingDao;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.dto.command.CreateReservationWaitingCommand;
import roomescape.reservationWaiting.dto.response.ReservationWaitingResponse;
import roomescape.reservationtime.dao.ReservationTimeDao;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;

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

    public ReservationWaitingResponse addReservationWaiting(CreateReservationWaitingCommand command) {
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());

        validateUniqueReservation(command.reservationDate(), command.timeId(), command.themeId());
        validateUniqueReservationWaiting(command.name(), command.reservationDate(), command.timeId(), command.themeId());
        validatePastDatetime(command.reservationDate(), reservationTime);

        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId(command.name(), LocalDateTime.now(), command.reservationDate(), reservationTime, theme);
        ReservationWaiting savedReservationWaiting = reservationWaitingDao.insert(reservationWaiting);

        int order = reservationWaitingDao.countOrder(command.reservationDate(), command.timeId(), command.themeId(), savedReservationWaiting.getId());
        return ReservationWaitingResponse.from(savedReservationWaiting, order);
    }

    public void delete(Long reservationWaitingId) {
        int deleted = reservationWaitingDao.delete(reservationWaitingId);
        if (deleted == 0) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.NOT_FOUND);
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

    private void validateUniqueReservation(LocalDate date, long timeId, long themeId) {
        boolean exists = reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (exists) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validateUniqueReservationWaiting(String name, LocalDate reservationDate, long timeId, long themeId) {
        boolean exists = reservationWaitingDao.existsByNameAndDateAndTimeIdAndThemeId(name, reservationDate, timeId, themeId);
        if (exists) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.DUPLICATE);
        }
    }

    private void validatePastDatetime(LocalDate date, ReservationTime reservationTime) {
        if (reservationTime.isPast(date, LocalDateTime.now())) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.PAST_DATETIME);
        }
    }
}
