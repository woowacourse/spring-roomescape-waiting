package roomescape.waiting.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.application.port.out.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.slot.application.SlotAssembler;
import roomescape.slot.domain.Slot;
import roomescape.slot.domain.SlotOccupancy;
import roomescape.waiting.application.dto.request.WaitingRequest;
import roomescape.waiting.application.dto.response.WaitingDetailFindResponse;
import roomescape.waiting.application.dto.response.WaitingResponse;
import roomescape.waiting.application.port.in.CancelWaitingUseCase;
import roomescape.waiting.application.port.in.CreateWaitingUseCase;
import roomescape.waiting.application.port.in.FindWaitingUseCase;
import roomescape.waiting.application.port.out.WaitingRepository;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingLine;
import roomescape.waiting.domain.WaitingLines;

@Service
@RequiredArgsConstructor
public class WaitingService implements CreateWaitingUseCase, CancelWaitingUseCase, FindWaitingUseCase {

    private final SlotAssembler slotAssembler;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public WaitingResponse save(WaitingRequest body, long memberId) {
        Member member = findMember(memberId);
        Slot slot = slotAssembler.assembleExisting(body.date(), body.timeId(), body.themeId());
        long slotId = slot.getId();

        validateReservedByMemberNotExists(memberId, slotId);
        validateWaitingByMemberNotExists(memberId, slotId);
        validateWaitingTargetExists(slotId);

        Waiting waiting = waitingRepository.save(Waiting.create(member, slot));
        WaitingLine waitingLine = WaitingLine.of(waitingRepository.findAllBySlotIdOrderById(slotId));
        long waitingOrder = waitingLine.orderOf(waiting);

        return WaitingResponse.of(waiting, waitingOrder);
    }

    private Member findMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.MEMBER_NOT_FOUND, memberId));
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

    @Transactional
    public void deleteById(long waitingId) {
        findWaitingForUpdate(waitingId);
        waitingRepository.deleteById(waitingId);
    }

    @Transactional
    public void deleteByIdForUser(long waitingId, long memberId) {
        Waiting waiting = findWaitingForUpdate(waitingId);

        waiting.validateOwnedBy(memberId);

        waitingRepository.deleteById(waitingId);
    }

    private Waiting findWaitingForUpdate(long waitingId) {
        return waitingRepository.findByIdForUpdate(waitingId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.WAITING_NOT_FOUND, waitingId));
    }

    public List<WaitingDetailFindResponse> findWaitingDetails() {
        List<WaitingDetailProjection> waitingDetails = waitingRepository.findAllWaitingDetails();
        WaitingLines waitingLines = findWaitingLines(waitingDetails);

        return waitingDetails.stream()
                .map(waitingDetail -> WaitingDetailFindResponse.from(
                        waitingDetail,
                        waitingLines.orderOf(waitingDetail.slotId(), waitingDetail.id())
                ))
                .toList();
    }

    private WaitingLines findWaitingLines(List<WaitingDetailProjection> waitingDetails) {
        List<Long> slotIds = waitingDetails.stream()
                .map(WaitingDetailProjection::slotId)
                .distinct()
                .toList();

        return WaitingLines.of(waitingRepository.findAllBySlotIds(slotIds));
    }

}
