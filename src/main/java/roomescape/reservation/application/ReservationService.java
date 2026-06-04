package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.Reservation;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.dto.response.ReservationSaveResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;
import roomescape.slot.SlotOccupancy;
import roomescape.slot.application.SlotService;
import roomescape.slot.Slot;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingLine;
import roomescape.waiting.infrastructure.WaitingRepository;
import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final SlotService slotService;
    private final Clock clock;

    public ReservationSaveResponse save(ReservationSaveRequest body, long memberId) {
        slotService.validateSlot(body.date(), body.timeId(), body.themeId());
        Slot slot = slotService.resolveSlot(body.date(), body.timeId(), body.themeId());
        throwIfSlotUnavailableForReservation(slot.getId());
        Reservation reservation = reservationRepository.save(Reservation.create(memberId, slot));

        return ReservationSaveResponse.from(reservation);
    }

    public List<ReservationDetailFindResponse> findReservationDetails() {
        return ReservationDetailFindResponse.from(reservationRepository.findAll());
    }

    public void deleteById(long reservationId) {
        deleteInternal(reservationId, oldReservation -> {
        });
    }

    public void deleteByIdForUser(long reservationId, long memberId) {
        deleteInternal(
                reservationId,
                oldReservation -> oldReservation.validateOwnedBy(memberId)
        );
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
        return updateInternal(
                body,
                reservationId,
                oldReservation -> oldReservation.validateOwnedBy(memberId)
        );
    }

    public ReservationSaveResponse update(ReservationUpdateRequest body, long reservationId) {
        return updateInternal(body, reservationId, oldReservation -> {
        });
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

    private void deleteInternal(
            long reservationId,
            Consumer<Reservation> accessValidator
    ) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElse(null);
        if (reservation == null) {
            return;
        }
        accessValidator.accept(reservation);
        validateNotPast(reservation);
        reservationRepository.deleteById(reservationId);
    }

    private ReservationSaveResponse updateInternal(
            ReservationUpdateRequest body,
            long reservationId,
            Consumer<Reservation> accessValidator
    ) {
        Reservation oldReservation = getOldReservationOrThrow(reservationId);
        accessValidator.accept(oldReservation);
        validateNotPast(oldReservation);
        validateNotEmptyUpdateRequest(body);

        LocalDate newDate = Objects.requireNonNullElse(body.date(), oldReservation.getSlot().getDate());
        long newTimeId = Objects.requireNonNullElse(body.timeId(), oldReservation.getSlot().getTimeId());
        long themeId = oldReservation.getSlot().getThemeId();
        slotService.validateSlot(newDate, newTimeId, themeId);
        Slot slot = slotService.resolveSlot(newDate, newTimeId, themeId);
        long slotId = slot.getId();
        throwIfSlotUnavailableForUpdate(reservationId, slotId);

        int affectedRow = reservationRepository.updateSlotById(oldReservation.getId(), slotId);
        validateReservationUpdated(affectedRow);

        return ReservationSaveResponse.from(getNewReservationOrThrow(reservationId));
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
