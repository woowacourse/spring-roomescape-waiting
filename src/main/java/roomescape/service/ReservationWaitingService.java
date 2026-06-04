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
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateReservationWaitingCommand;
import roomescape.dto.response.ReservationWaitingResponse;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
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
        ReservationSlot slot = new ReservationSlot(command.reservationDate(), reservationTime, theme);

        lockReservationForSlot(slot);
        validateUniqueReservationWaiting(command.name(), slot);
        validateNotDuplicateWithReservation(command.name(), slot);
        validatePastDatetime(slot, now);

        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId(command.name(), now, slot);
        ReservationWaiting savedReservationWaiting = reservationWaitingDao.insert(reservationWaiting);

        int order = reservationWaitingDao.countOrder(slot, savedReservationWaiting.getId());
        return ReservationWaitingResponse.from(savedReservationWaiting, order);
    }

    @Transactional
    public void convertFirstWaitingToReservation(ReservationSlot slot) {
        reservationWaitingDao.selectFirstBySlot(slot).ifPresent(waiting -> {
            reservationDao.insert(Reservation.createWithoutId(waiting.getName(), slot));
            reservationWaitingDao.delete(waiting.getId());
        });
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

    private void lockReservationForSlot(ReservationSlot slot) {
        reservationDao.findBySlotForUpdate(slot)
                .orElseThrow(() -> new RoomEscapeException(ReservationWaitingErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateUniqueReservationWaiting(String name, ReservationSlot slot) {
        boolean exists = reservationWaitingDao.existsByNameAndSlot(name, slot);
        if (exists) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.DUPLICATE);
        }
    }

    private void validateNotDuplicateWithReservation(String name, ReservationSlot slot) {
        boolean exists = reservationDao.existsByNameAndSlot(name, slot);
        if (exists) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.ALREADY_RESERVED);
        }
    }

    private void validatePastDatetime(ReservationSlot slot, LocalDateTime now) {
        if (slot.isPast(now)) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.PAST_DATETIME);
        }
    }
}
