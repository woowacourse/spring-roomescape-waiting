package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.SlotDao;
import roomescape.dao.WaitingDao;
import roomescape.dao.dto.RankedWaiting;
import roomescape.domain.Slot;
import roomescape.domain.Waiting;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.dto.response.WaitingWithRankResponse;
import roomescape.exception.code.SlotErrorCode;
import roomescape.exception.code.WaitingErrorCode;
import roomescape.exception.domain.SlotException;
import roomescape.exception.domain.WaitingException;


@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;
    private final SlotDao slotDao;

    public WaitingService(WaitingDao waitingDao, ReservationDao reservationDao, SlotDao slotDao) {
        this.waitingDao = waitingDao;
        this.reservationDao = reservationDao;
        this.slotDao = slotDao;
    }

    @Transactional
    public WaitingResponse create(WaitingRequest request, LocalDateTime currentDateTime) {
        Slot slot = slotDao.findByDateAndTimeAndTheme(request.date(), request.timeId(), request.themeId())
                .orElseThrow(() -> new SlotException(SlotErrorCode.SLOT_NOT_FOUND));

        validateReservationExists(slot.getId());
        validateNotOwnReservation(slot.getId(), request.name());

        Waiting waiting = new Waiting(currentDateTime, slot.getId(), request.name());
        validateUniqueWaiting(slot.getId(), request.name());
        Waiting savedWaiting = waitingDao.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    private void validateReservationExists(long slotId) {
        if (!reservationDao.existsBySlotIdForUpdate(slotId)) {
            throw new WaitingException(WaitingErrorCode.RESERVATION_REQUIRED_FOR_WAITING);
        }
    }

    private void validateNotOwnReservation(long slotId, String name) {
        if (reservationDao.existsBySlotIdAndName(slotId, name)) {
            throw new WaitingException(WaitingErrorCode.CANNOT_WAIT_OWN_RESERVATION);
        }
    }

    private void validateUniqueWaiting(long slotId, String name) {
        if (waitingDao.existsBySlotAndName(slotId, name)) {
            throw new WaitingException(WaitingErrorCode.WAITING_ALREADY_EXISTS);
        }
    }

    public List<WaitingWithRankResponse> getWaitingsByName(String name) {
        List<RankedWaiting> allWithRankByName = waitingDao.findAllWithRankByName(name);
        return allWithRankByName.stream()
                .map(WaitingWithRankResponse::from)
                .toList();
    }

    @Transactional
    public void delete(long waitingId, String name) {
        Waiting waiting = waitingDao.findById(waitingId)
                .orElseThrow(() -> new WaitingException(WaitingErrorCode.WAITING_NOT_FOUND));

        if (!waiting.getName().equals(name)) {
            throw new WaitingException(WaitingErrorCode.WAITING_NOT_FOUND);
        }
        waitingDao.delete(waitingId);
    }
}
