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
import roomescape.reservation.Reservation;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.dto.response.ReservationSaveResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.slot.SlotOccupancy;
import roomescape.slot.Slot;
import roomescape.slot.application.SlotAssembler;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingLine;
import roomescape.waiting.WaitingLines;
import roomescape.waiting.WaitingPromotionPolicy;
import roomescape.waiting.infrastructure.WaitingRepository;
import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final SlotAssembler slotAssembler;
    private final WaitingPromotionPolicy waitingPromotionPolicy;
    private final Clock clock;

    public ReservationSaveResponse save(ReservationSaveRequest body, long memberId) {
        Slot slot = slotAssembler.assembleExisting(body.date(), body.timeId(), body.themeId());
        throwIfSlotUnavailableForReservation(slot.getId());
        Reservation reservation = reservationRepository.save(Reservation.create(memberId, slot));

        return ReservationSaveResponse.from(reservation);
    }

    public List<ReservationDetailFindResponse> findReservationDetails() {
        return ReservationDetailFindResponse.from(reservationRepository.findAll());
    }

    @Transactional
    public void deleteById(long reservationId) {
        findReservationIfExists(reservationId)
                .ifPresent(this::cancelReservation);
    }

    @Transactional
    public void deleteByIdForUser(long reservationId, long memberId) {
        findReservationIfExists(reservationId)
                .ifPresent(reservation -> cancelReservationByUser(reservation, memberId));
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
                        waitingOrderOf(memberId, waitingDetail, waitingLines)
                ))
                .toList();
    }

    private List<ReservationDetailFindResponse> mergeMyReservations(
            List<ReservationDetailFindResponse> reservations,
            List<ReservationDetailFindResponse> waitings
    ) {
        return Stream.concat(reservations.stream(), waitings.stream())
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
            long memberId,
            WaitingDetailProjection waitingDetail,
            WaitingLines waitingLines
    ) {
        Waiting waiting = Waiting.of(waitingDetail.id(), memberId, waitingDetail.slotId());
        return waitingLines.orderOf(waiting);
    }

    private Optional<Reservation> findReservationIfExists(long reservationId) {
        return reservationRepository.findById(reservationId);
    }

    private void cancelReservationByUser(Reservation reservation, long memberId) {
        reservation.validateOwnedBy(memberId);
        cancelReservation(reservation);
    }

    private void cancelReservation(Reservation reservation) {
        validateCancelable(reservation);

        WaitingLine waitingLine = findWaitingLineFor(reservation);
        deleteReservationOnly(reservation);
        promoteFirstWaitingIfExists(reservation, waitingLine);
    }

    private WaitingLine findWaitingLineFor(Reservation reservation) {
        return WaitingLine.of(waitingRepository.findAllBySlotIdOrderById(reservation.getSlotId()));
    }

    private void deleteReservationOnly(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
    }

    private void promoteFirstWaitingIfExists(Reservation canceledReservation, WaitingLine waitingLine) {
        waitingLine.first()
                .ifPresent(waiting -> {
                    Reservation promotedReservation = waitingPromotionPolicy.promote(waiting, canceledReservation.getSlot());
                    reservationRepository.save(promotedReservation);
                    waitingRepository.deleteById(waiting.getId());
                });
    }

    private void throwIfSlotUnavailableForReservation(long slotId) {
        boolean hasReservation = reservationRepository.existsBySlotId(slotId);
        boolean hasWaiting = waitingRepository.existsBySlotId(slotId);
        SlotOccupancy slotOccupancy = SlotOccupancy.of(hasReservation, hasWaiting);

        if (!slotOccupancy.isReservable()) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_AVAILABLE, slotId);
        }
    }

    private void validateCancelable(Reservation reservation) {
        reservation.validateNotPast(LocalDateTime.now(clock));
    }
}
