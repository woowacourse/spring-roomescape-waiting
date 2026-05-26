package roomescape.waiting.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
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

    public WaitingResponse save(WaitingRequest body, long memberId) {
        scheduleService.validateSchedule(body.date(), body.timeId(), body.themeId());
        long scheduleId = scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(body.date(), body.timeId(), body.themeId());

        if (waitingRepository.existsByScheduleIdAndMemberId(memberId, scheduleId)){
            throw new EscapeRoomException(ErrorCode.INVALID_INPUT);
        }

        Waiting waiting = waitingRepository.save(body.toDomain(memberId, scheduleId));
        long waitingOrder = waitingRepository.countByScheduleIdAndIdLessThanEqual(scheduleId, waiting.getId());

        return WaitingResponse.of(waiting, waitingOrder);
    }
}
