package roomescape.waiting.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.slot.domain.Slot;
import roomescape.slot.domain.SlotOccupancy;
import roomescape.slot.application.SlotAssembler;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingLine;
import roomescape.waiting.application.dto.request.WaitingRequest;
import roomescape.waiting.application.dto.response.WaitingResponse;
import roomescape.waiting.application.port.out.WaitingRepository;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final SlotAssembler slotAssembler;
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public WaitingResponse save(WaitingRequest body, long memberId) {
        Slot slot = slotAssembler.assembleExisting(body.date(), body.timeId(), body.themeId());
        long slotId = slot.getId();

        validateReservedByMemberNotExists(memberId, slotId);
        validateWaitingByMemberNotExists(memberId, slotId);
        validateWaitingTargetExists(slotId);

        Waiting waiting = waitingRepository.save(Waiting.create(memberId, slotId));
        WaitingLine waitingLine = WaitingLine.of(waitingRepository.findAllBySlotIdOrderById(slotId));
        long waitingOrder = waitingLine.orderOf(waiting);

        return WaitingResponse.of(waiting, waitingOrder);
    }

    public void deleteByIdForUser(long waitingId, long memberId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElse(null);
        if (waiting == null) {
            return;
        }

        waiting.validateOwnedBy(memberId);

        waitingRepository.deleteById(waitingId);
    }

    private void validateReservedByMemberNotExists(long memberId, long slotId) {
        if (reservationRepository.existsByMemberIdAndSlotId(memberId, slotId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION);
        }
    }

    private void validateWaitingByMemberNotExists(long memberId, long slotId) {
        if (waitingRepository.existsBySlotIdAndMemberId(memberId, slotId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_ALREADY_EXIST);
        }
    }

    private void validateWaitingTargetExists(long slotId) {
        SlotOccupancy slotOccupancy = SlotOccupancy.of(
                reservationRepository.existsBySlotId(slotId),
                waitingRepository.existsBySlotId(slotId)
        );

        if (!slotOccupancy.isWaitable()) {
            throw new EscapeRoomException(ErrorCode.WAITING_TARGET_BAD_REQUEST);
        }
    }
}
