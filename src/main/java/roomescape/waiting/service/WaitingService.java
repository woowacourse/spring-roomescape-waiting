package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.response.MyReservationAndWaitingResponse;
import roomescape.schedule.domain.Schedule;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.repository.WaitingRepository;

import java.util.List;

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

    public List<MyReservationAndWaitingResponse> findWaitingWithRankByMemberId(Long memberId) {
        return waitingRepository.findWaitingWithRankByMemberId(memberId).stream()
                .map(MyReservationAndWaitingResponse::fromWaitingAndStatus)
                .toList();
    }

    public void deleteWaitingById(Long id) {
        waitingRepository.deleteById(id);
    }

    public boolean existsByMemberAndSchedule(Member member, Schedule schedule) {
        return waitingRepository.existsByMemberAndSchedule(member, schedule);
    }

    public List<Waiting> findAll() {
        return waitingRepository.findAll();
    }
}
