package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
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
import roomescape.slot.application.SlotService;
import roomescape.waiting.infrastructure.WaitingRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final SlotService slotService;

    public ReservationSaveResponse save(ReservationSaveRequest body, long memberId) {
        slotService.validateSlot(body.date(), body.timeId(), body.themeId());
        long slotId = slotService.resolveSlotId(body.date(), body.timeId(),
                body.themeId());
        throwIfSlotUnavailableForReservation(slotId);
        Reservation reservation = reservationRepository.save(body.toDomain(memberId, slotId));

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
                oldReservation -> validateReservationOwner(reservationId, oldReservation, memberId)
        );
    }

    public List<ReservationDetailFindResponse> findMyReservations(long memberId) {
        return ReservationDetailFindResponse.merge(
                reservationRepository.findAllReservationDetailsByMemberId(memberId),
                waitingRepository.findAllWaitingDetailsByMemberId(memberId)
        );
    }

    public ReservationSaveResponse updateForUser(ReservationUpdateRequest body, long reservationId, long memberId) {
        return updateInternal(
                body,
                reservationId,
                oldReservation -> validateReservationOwner(reservationId, oldReservation, memberId)
        );
    }

    public ReservationSaveResponse update(ReservationUpdateRequest body, long reservationId) {
        return updateInternal(body, reservationId, oldReservation -> {
        });
    }

    private static void validateReservationOwner(
            long reservationId,
            ReservationDetailProjection reservationDetail,
            long memberId
    ) {
        if (!Objects.equals(reservationDetail.memberId(), memberId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_OWNED_BY_MEMBER, reservationId);
        }
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
            Consumer<ReservationDetailProjection> accessValidator
    ) {
        ReservationDetailProjection reservationDetail = reservationRepository.findDetailById(reservationId)
                .orElse(null);
        if (reservationDetail == null) {
            return;
        }
        accessValidator.accept(reservationDetail);
        validateNotPast(reservationDetail);
        reservationRepository.deleteById(reservationId);
    }

    private ReservationSaveResponse updateInternal(
            ReservationUpdateRequest body,
            long reservationId,
            Consumer<ReservationDetailProjection> accessValidator
    ) {
        ReservationDetailProjection oldReservation = getOldReservationDetailOrThrow(reservationId);
        accessValidator.accept(oldReservation);
        validateNotPast(oldReservation);
        validateNotEmptyUpdateRequest(body);

        LocalDate newDate = Objects.requireNonNullElse(body.date(), oldReservation.date());
        long newTimeId = Objects.requireNonNullElse(body.timeId(), oldReservation.getTimeId());
        long slotId = slotService.resolveSlotId(newDate, newTimeId,
                oldReservation.getThemeId());
        slotService.validateSlot(newDate, newTimeId, oldReservation.getThemeId());
        throwIfSlotUnavailableForUpdate(reservationId, slotId);

        int affectedRow = reservationRepository.updateSlotById(oldReservation.id(), slotId);
        validateReservationUpdated(affectedRow);

        return ReservationSaveResponse.from(getNewReservationOrThrow(reservationId));
    }

    private Reservation getNewReservationOrThrow(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(
                        () -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND_AFTER_UPDATE, reservationId));
    }

    private ReservationDetailProjection getOldReservationDetailOrThrow(long reservationId) {
        return reservationRepository.findDetailById(reservationId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND, reservationId));
    }

    private void throwIfSlotUnavailableForUpdate(long reservationId, long slotId) {
        if (reservationRepository.existsBySlotIdAndIdNot(slotId, reservationId)
                || waitingRepository.existsBySlotId(slotId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_AVAILABLE, slotId);
        }
    }

    private void throwIfSlotUnavailableForReservation(long slotId) {
        if (reservationRepository.existsBySlotId(slotId)
                || waitingRepository.existsBySlotId(slotId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_AVAILABLE, slotId);
        }
    }

    private void validateNotPast(ReservationDetailProjection reservationDetail) {
        slotService.validateNotPastDate(reservationDetail.date());
        slotService.validateNotPastTime(reservationDetail.date(), reservationDetail.getTime());
    }
}
