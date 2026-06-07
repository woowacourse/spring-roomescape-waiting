package roomescape.waiting.application;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.ReservationPromotionService;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.schedule.application.ScheduleService;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingRepository;
import roomescape.waiting.dto.request.WaitingRequest;
import roomescape.waiting.dto.response.WaitingResponse;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final ScheduleService scheduleService;
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationPromotionService reservationPromotionService;

    @Transactional
    public WaitingResponse save(WaitingRequest body, long memberId) {
        scheduleService.validateSchedule(body.date(), body.timeId(), body.themeId());
        long scheduleId = scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(body.date(), body.timeId(), body.themeId());

        validateMemberNotAlreadyReserved(memberId, scheduleId);
        validateMemberNotAlreadyWaiting(memberId, scheduleId);
        validateWaitingTargetExists(scheduleId);

        if (body.reservationIdToCancel() != null) {
            cancelReservationAndPromoteFirstWaiting(body.reservationIdToCancel(), memberId);
        }

        Waiting waiting = saveWaiting(body.toDomain(memberId, scheduleId));
        long waitingOrder = waitingRepository.countByScheduleIdAndIdLessThanEqual(scheduleId, waiting.getId());

        return WaitingResponse.of(waiting, waitingOrder);
    }

    @Transactional
    public void cancelByIdForUser(long waitingId, long memberId) {
        Waiting waiting = waitingRepository.findByIdForPromotion(waitingId)
                .orElse(null);
        if (waiting == null) {
            return;
        }

        if (!waiting.isSameMemberId(memberId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_NOT_OWNED_BY_MEMBER, waitingId);
        }

        waitingRepository.deleteById(waitingId);
    }

    private Waiting saveWaiting(Waiting waiting) {
        try {
            return waitingRepository.save(waiting);
        } catch (DuplicateKeyException e) {
            throw new EscapeRoomException(ErrorCode.WAITING_ALREADY_EXIST);
        }
    }

    private void validateMemberNotAlreadyReserved(long memberId, long scheduleId) {
        if (reservationRepository.existsByMemberIdAndScheduleId(memberId, scheduleId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_ON_OWN_RESERVATION_NOT_ALLOWED);
        }
    }

    private void validateMemberNotAlreadyWaiting(long memberId, long scheduleId) {
        if (waitingRepository.existsByScheduleIdAndMemberId(scheduleId, memberId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_ALREADY_EXIST);
        }
    }

    private void validateWaitingTargetExists(long scheduleId) {
        if (reservationRepository.findByScheduleIdForPromotion(scheduleId).isEmpty()
                && !waitingRepository.existsByScheduleId(scheduleId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_TARGET_BAD_REQUEST);
        }
    }

    private void cancelReservationAndPromoteFirstWaiting(long reservationId, long memberId) {
        Reservation reservation = reservationRepository.findByIdForPromotion(reservationId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND, reservationId));

        if (!reservation.isSameMemberId(memberId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_OWNED_BY_MEMBER, reservationId);
        }

        reservationPromotionService.cancelReservationAndPromoteFirstWaiting(reservation);
    }
}
