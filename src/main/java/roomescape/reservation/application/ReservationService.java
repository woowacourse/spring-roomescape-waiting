package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.application.port.out.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.application.dto.request.ReservationSaveRequest;
import roomescape.reservation.application.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.application.dto.response.ReservationSaveResponse;
import roomescape.reservation.application.port.in.CancelReservationUseCase;
import roomescape.reservation.application.port.in.CreateReservationUseCase;
import roomescape.reservation.application.port.in.FindReservationUseCase;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.slot.application.SlotAssembler;
import roomescape.slot.domain.Slot;
import roomescape.slot.domain.SlotOccupancy;
import roomescape.waiting.application.port.out.WaitingRepository;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingLine;
import roomescape.waiting.domain.WaitingLines;
import roomescape.waiting.domain.WaitingPromotionPolicy;

@Service
@RequiredArgsConstructor
public class ReservationService implements CreateReservationUseCase, FindReservationUseCase, CancelReservationUseCase {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;
    private final SlotAssembler slotAssembler;
    private final WaitingPromotionPolicy waitingPromotionPolicy;
    private final Clock clock;

    public ReservationSaveResponse save(ReservationSaveRequest body, long memberId) {
        Member member = findMember(memberId);
        Slot slot = slotAssembler.assembleExisting(body.date(), body.timeId(), body.themeId());
        throwIfSlotUnavailableForReservation(slot.getId());
        Reservation reservation = reservationRepository.save(Reservation.create(member, slot));

        return ReservationSaveResponse.from(reservation);
    }

    private Member findMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.MEMBER_NOT_FOUND, memberId));
    }

    private void throwIfSlotUnavailableForReservation(long slotId) {
        boolean hasReservation = reservationRepository.existsBySlotId(slotId);
        boolean hasWaiting = waitingRepository.existsBySlotId(slotId);
        SlotOccupancy slotOccupancy = SlotOccupancy.of(hasReservation, hasWaiting);

        if (!slotOccupancy.isReservable()) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_AVAILABLE, slotId);
        }
    }

    public List<ReservationDetailFindResponse> findReservationDetails() {
        return ReservationDetailFindResponse.from(reservationRepository.findAll());
    }

    @Transactional
    public void deleteById(long reservationId) {
        findReservationIfExists(reservationId)
                .ifPresent(this::cancelReservation);
    }

    private Optional<Reservation> findReservationIfExists(long reservationId) {
        return reservationRepository.findById(reservationId);
    }

    @Transactional
    public void deleteByIdForUser(long reservationId, long memberId) {
        findReservationIfExists(reservationId)
                .ifPresent(reservation -> cancelReservationByUser(reservation, memberId));
    }

    private void cancelReservationByUser(Reservation reservation, long memberId) {
        reservation.validateOwnedBy(memberId);
        cancelReservation(reservation);
    }

    private void cancelReservation(Reservation reservation) {
        reservation.validateCancelable(LocalDateTime.now(clock));

        WaitingLine waitingLine = findWaitingLineFor(reservation);
        deleteReservationOnly(reservation);
        promoteFirstWaitingIfExists(reservation, waitingLine);
    }

    private WaitingLine findWaitingLineFor(Reservation reservation) {
        return WaitingLine.of(waitingRepository.findAllBySlotIdOrderByIdForUpdate(reservation.getSlotId()));
    }

    private void deleteReservationOnly(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
    }

    private void promoteFirstWaitingIfExists(Reservation canceledReservation, WaitingLine waitingLine) {
        waitingLine.first()
                .ifPresent(waiting -> {
                    Reservation promotedReservation = waitingPromotionPolicy.promote(waiting,
                            canceledReservation.getSlot());
                    reservationRepository.save(promotedReservation);
                    waitingRepository.deleteById(waiting.getId());
                });
    }

    public List<ReservationDetailFindResponse> findMyReservations(long memberId) {
        List<ReservationDetailFindResponse> reservations = findMyReservationResponses(memberId);
        List<ReservationDetailFindResponse> waitings = findMyWaitingResponses(memberId);

        return mergeMyReservations(reservations, waitings);
    }

    private List<ReservationDetailFindResponse> findMyReservationResponses(long memberId) {
        return reservationRepository.findAllReservationDetailsByMemberId(memberId)
                .stream()
                .map(ReservationDetailFindResponse::from)
                .toList();
    }

    private List<ReservationDetailFindResponse> findMyWaitingResponses(long memberId) {
        List<WaitingDetailProjection> waitingDetails = waitingRepository.findAllWaitingDetailsByMemberId(memberId);
        WaitingLines waitingLines = findWaitingLines(waitingDetails);

        return waitingDetails.stream()
                .map(waitingDetail -> ReservationDetailFindResponse.from(
                        waitingDetail,
                        waitingOrderOf(waitingDetail, waitingLines)
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

    private long waitingOrderOf(
            WaitingDetailProjection waitingDetail,
            WaitingLines waitingLines
    ) {
        return waitingLines.orderOf(waitingDetail.slotId(), waitingDetail.id());
    }

    private List<ReservationDetailFindResponse> mergeMyReservations(
            List<ReservationDetailFindResponse> reservations,
            List<ReservationDetailFindResponse> waitings
    ) {
        return Stream.concat(reservations.stream(), waitings.stream())
                .toList();
    }

}
