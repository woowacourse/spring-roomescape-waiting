package roomescape.waiting.application;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.slot.application.SlotService;
import roomescape.waiting.Waiting;
import roomescape.waiting.infrastructure.WaitingRepository;
import roomescape.waiting.dto.request.WaitingRequest;
import roomescape.waiting.dto.response.WaitingResponse;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final SlotService slotService;
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public WaitingResponse save(WaitingRequest body, long memberId) {
        slotService.validateSlot(body.date(), body.timeId(), body.themeId());
        long slotId = slotService.resolveSlotId(body.date(), body.timeId(), body.themeId());

        validateReservedByMemberNotExists(memberId, slotId);
        validateWaitingByMemberNotExists(memberId, slotId);
        validateWaitingTargetExists(slotId);

        Waiting waiting = waitingRepository.save(body.toDomain(memberId, slotId));
        long waitingOrder = waitingRepository.countBySlotIdAndIdLessThanEqual(slotId, waiting.getId());

        return WaitingResponse.of(waiting, waitingOrder);
    }

    public void deleteByIdForUser(long waitingId, long memberId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElse(null);
        if(waiting == null) {
            return;
        }

        if (!Objects.equals(waiting.getMemberId(), memberId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_NOT_OWNED_BY_MEMBER, waitingId);
        }

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
        if (!reservationRepository.existsBySlotId(slotId)
                && !waitingRepository.existsBySlotId(slotId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_TARGET_BAD_REQUEST);
        }
    }
}
