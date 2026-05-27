package roomescape.reservation.application;

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
import roomescape.schedule.application.ScheduleService;
import roomescape.waiting.infrastructure.WaitingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ScheduleService scheduleService;

    public ReservationSaveResponse save(ReservationSaveRequest body, long memberId) {
        scheduleService.validateSchedule(body.date(), body.timeId(), body.themeId());
        long scheduleId = scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(body.date(), body.timeId(), body.themeId());
        validateReservationAlreadyExistsNot(scheduleId);
        Reservation reservation = reservationRepository.save(body.toDomain(memberId, scheduleId));

        return ReservationSaveResponse.from(reservation);
    }

    public List<ReservationDetailFindResponse> findReservationDetails() {
        return ReservationDetailFindResponse.from(reservationRepository.findAll());
    }

    public void deleteById(long reservationId) {
        deleteInternal(reservationId, oldReservation -> {});
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
        return updateInternal(body, reservationId, oldReservation -> {});
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
        long scheduleId = scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(newDate, newTimeId, oldReservation.getThemeId());
        scheduleService.validateSchedule(newDate, newTimeId, oldReservation.getThemeId());
        validateDuplicatedReservationNot(reservationId, scheduleId);

        int affectedRow = reservationRepository.updateScheduleById(oldReservation.id(), scheduleId);
        validateReservationUpdated(affectedRow);

        return ReservationSaveResponse.from(getNewReservationOrThrow(reservationId));
    }

    private Reservation getNewReservationOrThrow(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND_AFTER_UPDATE, reservationId));
    }

    private ReservationDetailProjection getOldReservationDetailOrThrow(long reservationId) {
        return reservationRepository.findDetailById(reservationId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND, reservationId));
    }

    private void validateDuplicatedReservationNot(long reservationId, long scheduleId) {
        if (reservationRepository.existsByScheduleIdAndIdNot(scheduleId, reservationId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_ALREADY_EXIST, scheduleId);
        }
    }

    private void validateReservationAlreadyExistsNot(long scheduleId) {
        if (reservationRepository.existsByScheduleId(scheduleId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_ALREADY_EXIST, scheduleId);
        }
    }

    private void validateNotPast(ReservationDetailProjection reservationDetail) {
        scheduleService.validateNotPastDate(reservationDetail.date());
        scheduleService.validateNotPastTime(reservationDetail.date(), reservationDetail.getTime());
    }
}
