package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.schedule.domain.Schedule;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;

    public WaitingService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public Waiting createWaiting(WaitingCreateRequest waitingCreateRequest, Schedule schedule, Member member) {
        Waiting waiting = waitingCreateRequest.toWaiting(schedule, member);
        return waitingRepository.save(waiting);
    }
}
