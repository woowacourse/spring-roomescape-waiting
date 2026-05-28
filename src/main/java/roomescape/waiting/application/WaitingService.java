package roomescape.waiting.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.schedule.application.ScheduleService;
import roomescape.waiting.Waiting;
import roomescape.waiting.dto.request.WaitingRequest;
import roomescape.waiting.dto.response.WaitingResponse;
import roomescape.waiting.infrastructure.WaitingRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final ScheduleService scheduleService;
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public WaitingResponse save(WaitingRequest body, long memberId) {
        scheduleService.validateSchedule(body.date(), body.timeId(), body.themeId());
        long scheduleId = scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(body.date(), body.timeId(), body.themeId());

        validateMemberNotAlreadyReserved(memberId, scheduleId);
        validateMemberNotAlreadyWaiting(memberId, scheduleId);
        validateWaitingTargetExists(scheduleId);

        Waiting waiting = waitingRepository.save(body.toDomain(memberId, scheduleId));
        long waitingOrder = waitingRepository.countByScheduleIdAndIdLessThanEqual(scheduleId, waiting.getId());

        return WaitingResponse.of(waiting, waitingOrder);
    }

    public void deleteByIdForUser(long waitingId, long memberId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElse(null);
        if (waiting == null) {
            return;
        }

        if (!Objects.equals(waiting.getMemberId(), memberId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_NOT_OWNED_BY_MEMBER, waitingId);
        }

        waitingRepository.deleteById(waitingId);
    }

    private void validateMemberNotAlreadyReserved(long memberId, long scheduleId) {
        if (reservationRepository.existsByMemberIdAndScheduleId(memberId, scheduleId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_ALREADY_EXIST);
        }
    }

    private void validateMemberNotAlreadyWaiting(long memberId, long scheduleId) {
        if (waitingRepository.existsByScheduleIdAndMemberId(memberId, scheduleId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_ALREADY_EXIST);
        }
    }

    private void validateWaitingTargetExists(long scheduleId) {
        if (!reservationRepository.existsByScheduleId(scheduleId)
                && !waitingRepository.existsByScheduleId(scheduleId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_TARGET_BAD_REQUEST);
        }
    }
}
