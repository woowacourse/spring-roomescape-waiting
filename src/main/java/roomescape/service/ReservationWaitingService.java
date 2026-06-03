package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
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
import roomescape.domain.*;
import roomescape.dto.command.CreateReservationWaitingCommand;
import roomescape.dto.response.ReservationWaitingResponse;

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
        ReservationSlot slot = new ReservationSlot(command.reservationDate(),reservationTime,theme);

        validateReservationExists(slot);
        slot.validateNotPast(now);
        validateUniqueReservationWaiting(command.name(), slot);

        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId(command.name(), now, slot.getDate(), reservationTime, theme);
        ReservationWaiting savedReservationWaiting;
        try {
            savedReservationWaiting = reservationWaitingDao.insert(reservationWaiting);
        } catch (DuplicateKeyException exception) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.DUPLICATE);
        }

        ReservationWaitingQueue waitings = new ReservationWaitingQueue(reservationWaitingDao.selectBySlot(slot));
        int order = waitings.orderOf(savedReservationWaiting);
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

    private void validateReservationExists(ReservationSlot slot) {
        boolean exists = reservationDao.existsByDateAndTimeIdAndThemeId(slot);
        if (!exists) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    private void validateUniqueReservationWaiting(String name, ReservationSlot slot) {
        boolean exists = reservationWaitingDao.existsByNameAndDateAndTimeIdAndThemeId(name, slot);
        if (exists) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.DUPLICATE);
        }
    }
}
