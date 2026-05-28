package roomescape.service;

import org.springframework.stereotype.Service;
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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class WaitingService {

    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;
    private final SlotDao slotDao;
    private final Clock clock;

    public WaitingService(WaitingDao waitingDao, ReservationDao reservationDao, SlotDao slotDao, Clock clock) {
        this.waitingDao = waitingDao;
        this.reservationDao = reservationDao;
        this.slotDao = slotDao;
        this.clock = clock;
    }

    public WaitingResponse create(WaitingRequest request) {
        Slot slot = slotDao.findByDateAndTimeAndTheme(request.date(), request.timeId(), request.themeId())
                .orElseThrow(() -> new SlotException(SlotErrorCode.SLOT_NOT_FOUND));

        validateNotOwnReservation(slot.getId(), request.name());

        Waiting waiting = new Waiting(LocalDateTime.now(clock), slot.getId(), request.name());
        validateUniqueWaiting(slot.getId(), request.name());
        Waiting savedWaiting = waitingDao.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    private void validateUniqueWaiting(long slotId, String name) {
        if (waitingDao.existsByCreatedAtAndSlotAndName(slotId, name)) {
            throw new WaitingException(WaitingErrorCode.WAITING_ALREADY_EXISTS);
        }
    }

    private void validateNotOwnReservation(long slotId, String name) {
        if (reservationDao.existsBySlotIdAndName(slotId, name)) {
            throw new WaitingException(WaitingErrorCode.CANNOT_WAIT_OWN_RESERVATION);
        }
    }

    public List<WaitingWithRankResponse> getWaitingsByName(String name) {
        List<RankedWaiting> allWithRankByName = waitingDao.findAllWithRankByName(name);
        return allWithRankByName.stream()
                .map(rankedWaiting -> new WaitingWithRankResponse(
                        rankedWaiting.id(),
                        rankedWaiting.createdAt(),
                        rankedWaiting.slotId(),
                        rankedWaiting.name(),
                        rankedWaiting.rank(),
                        rankedWaiting.date(),
                        rankedWaiting.startAt(),
                        rankedWaiting.themeName()))
                .toList();
    }

    public void delete(long waitingId) {
        waitingDao.delete(waitingId);
    }
}
