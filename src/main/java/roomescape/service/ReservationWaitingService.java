package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ReservationWaitingErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.dao.*;
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
    private final ReservationSlotDao reservationSlotDao;

    public ReservationWaitingService(ReservationWaitingDao reservationWaitingDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao, ReservationDao reservationDao, ReservationSlotDao reservationSlotDao) {
        this.reservationWaitingDao = reservationWaitingDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
        this.reservationSlotDao = reservationSlotDao;
    }

    @Transactional
    public ReservationWaitingResponse addReservationWaiting(CreateReservationWaitingCommand command, LocalDateTime now) {
        ReservationSlot slot = createReservationSlot(command);
        ReservationSlot savedSlot = reservationSlotDao.findOrCreate(slot);

        validateWaitingCreatable(command.name(), savedSlot, now);

        ReservationWaiting savedWaiting = saveReservationWaiting(command.name(), now, savedSlot);
        int order = calculateWaitingOrder(savedSlot, savedWaiting);

        return ReservationWaitingResponse.from(savedWaiting, order);
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

    private void validateNotReservedBySameUser(String name, ReservationSlot slot) {
        if (reservationDao.existsByNameAndDateAndTimeIdAndThemeId(name, slot)) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.ALREADY_RESERVED);
        }
    }

    private ReservationSlot createReservationSlot(CreateReservationWaitingCommand command) {
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());

        return new ReservationSlot(command.reservationDate(), reservationTime, theme);
    }

    private void validateWaitingCreatable(String name, ReservationSlot slot, LocalDateTime now) {
        validateReservationExists(slot);
        slot.validateNotPast(now);
        validateNotReservedBySameUser(name, slot);
        validateUniqueReservationWaiting(name, slot);
    }

    private ReservationWaiting saveReservationWaiting(String name, LocalDateTime now, ReservationSlot slot) {
        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId(
                name,
                now,
                slot
        );

        try {
            return reservationWaitingDao.insert(reservationWaiting);
        } catch (DuplicateKeyException exception) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.DUPLICATE);
        }
    }

    private int calculateWaitingOrder(ReservationSlot slot, ReservationWaiting savedWaiting) {
        ReservationWaitingQueue waitings = new ReservationWaitingQueue(reservationWaitingDao.selectBySlot(slot));
        return waitings.orderOf(savedWaiting);
    }
}
