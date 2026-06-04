package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.Reservation;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.dto.response.ReservationSaveResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.slot.SlotOccupancy;
import roomescape.slot.Slot;
import roomescape.slot.application.SlotAssembler;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingLine;
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
        Reservation reservation = findReservationOrNull(reservationId);
        if (reservation == null) {
            return;
        }
        deleteReservation(reservation);
    }

    @Transactional
    public void deleteByIdForUser(long reservationId, long memberId) {
        Reservation reservation = findReservationOrNull(reservationId);
        if (reservation == null) {
            return;
        }
        reservation.validateOwnedBy(memberId);
        deleteReservation(reservation);
    }

    public List<ReservationDetailFindResponse> findMyReservations(long memberId) {
        List<WaitingDetailProjection> waitingDetails = waitingRepository.findAllWaitingDetailsByMemberId(memberId);
        Map<Long, WaitingLine> waitingLinesBySlotId = createWaitingLinesBySlotId(waitingDetails);

        return ReservationDetailFindResponse.merge(
                reservationRepository.findAllReservationDetailsByMemberId(memberId),
                waitingDetails,
                waitingDetail -> waitingOrderOf(memberId, waitingDetail, waitingLinesBySlotId)
        );
    }

    private Map<Long, WaitingLine> createWaitingLinesBySlotId(List<WaitingDetailProjection> waitingDetails) {
        return waitingDetails.stream()
                .map(WaitingDetailProjection::slotId)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        slotId -> WaitingLine.of(waitingRepository.findAllBySlotIdOrderById(slotId))
                ));
    }

    private long waitingOrderOf(
            long memberId,
            WaitingDetailProjection waitingDetail,
            Map<Long, WaitingLine> waitingLinesBySlotId
    ) {
        Waiting waiting = Waiting.of(waitingDetail.id(), memberId, waitingDetail.slotId());
        return waitingLinesBySlotId.get(waitingDetail.slotId()).orderOf(waiting);
    }

    public ReservationSaveResponse updateForUser(ReservationUpdateRequest body, long reservationId, long memberId) {
        Reservation oldReservation = getOldReservationOrThrow(reservationId);
        oldReservation.validateOwnedBy(memberId);
        return updateReservation(body, oldReservation);
    }

    public ReservationSaveResponse update(ReservationUpdateRequest body, long reservationId) {
        Reservation oldReservation = getOldReservationOrThrow(reservationId);
        return updateReservation(body, oldReservation);
    }

    private static void validateReservationUpdated(int affectedRow) {
        if (affectedRow != 1) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_UPDATE_FAILED);
        }
    }

    private static void validateNotEmptyUpdateRequest(ReservationUpdateRequest body) {
        if (body.date() == null && body.timeId() == null) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_UPDATE_EMPTY);
        }
    }

    private Reservation findReservationOrNull(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElse(null);
    }

    private void deleteReservation(Reservation reservation) {
        validateNotPast(reservation);
        WaitingLine waitingLine = WaitingLine.of(waitingRepository.findAllBySlotIdOrderById(reservation.getSlotId()));
        reservationRepository.deleteById(reservation.getId());
        promoteFirstWaitingIfExists(reservation, waitingLine);
    }

    private void promoteFirstWaitingIfExists(Reservation canceledReservation, WaitingLine waitingLine) {
        waitingLine.first()
                .ifPresent(waiting -> {
                    Reservation promotedReservation = waitingPromotionPolicy.promote(waiting, canceledReservation.getSlot());
                    reservationRepository.save(promotedReservation);
                    waitingRepository.deleteById(waiting.getId());
                });
    }

    private ReservationSaveResponse updateReservation(ReservationUpdateRequest body, Reservation oldReservation) {
        validateNotPast(oldReservation);
        validateNotEmptyUpdateRequest(body);

        LocalDate newDate = Objects.requireNonNullElse(body.date(), oldReservation.getSlot().getDate());
        long newTimeId = Objects.requireNonNullElse(body.timeId(), oldReservation.getSlot().getTimeId());
        long themeId = oldReservation.getSlot().getThemeId();
        Slot slot = slotAssembler.assembleExisting(newDate, newTimeId, themeId);
        long slotId = slot.getId();
        throwIfSlotUnavailableForUpdate(oldReservation.getId(), slotId);

        int affectedRow = reservationRepository.updateSlotById(oldReservation.getId(), slotId);
        validateReservationUpdated(affectedRow);

        return ReservationSaveResponse.from(getNewReservationOrThrow(oldReservation.getId()));
    }

    private Reservation getNewReservationOrThrow(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(
                        () -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND_AFTER_UPDATE, reservationId));
    }

    private Reservation getOldReservationOrThrow(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND, reservationId));
    }

    private void throwIfSlotUnavailableForUpdate(long reservationId, long slotId) {
        boolean hasOtherReservation = reservationRepository.existsBySlotIdAndIdNot(slotId, reservationId);
        boolean hasWaiting = waitingRepository.existsBySlotId(slotId);
        SlotOccupancy slotOccupancy = SlotOccupancy.of(hasOtherReservation, hasWaiting);

        if (!slotOccupancy.isReservable()) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_AVAILABLE, slotId);
        }
    }

    private void throwIfSlotUnavailableForReservation(long slotId) {
        boolean hasReservation = reservationRepository.existsBySlotId(slotId);
        boolean hasWaiting = waitingRepository.existsBySlotId(slotId);
        SlotOccupancy slotOccupancy = SlotOccupancy.of(hasReservation, hasWaiting);

        if (!slotOccupancy.isReservable()) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_AVAILABLE, slotId);
        }
    }

    private void validateNotPast(Reservation reservation) {
        reservation.validateNotPast(LocalDateTime.now(clock));
    }
}
