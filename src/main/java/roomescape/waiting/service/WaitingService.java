package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.response.MyReservationAndWaitingResponse;
import roomescape.schedule.domain.Schedule;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.repository.WaitingRepository;

import java.util.List;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;

    public WaitingService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public Waiting createWaiting(WaitingCreateRequest waitingCreateRequest, Schedule schedule, Member member) {
        Waiting waiting = waitingCreateRequest.toWaiting(schedule, member);
        return waitingRepository.save(waiting);
    }

    @Transactional(readOnly = true)
    public List<MyReservationAndWaitingResponse> getMyReservationAndWaitingResponseByMemberId(Long memberId) {
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingWithRankByMemberId(memberId);
        return convertMyReservationAndWaitingResponseTo(waitingWithRanks);
    }

    private List<MyReservationAndWaitingResponse> convertMyReservationAndWaitingResponseTo(List<WaitingWithRank> waitingWithRanks) {
        return waitingWithRanks.stream()
                .map(MyReservationAndWaitingResponse::fromWaitingAndStatus)
                .toList();
    }

    @Transactional
    public void deleteWaitingById(Long id) {
        waitingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByMemberAndSchedule(Member member, Schedule schedule) {
        return waitingRepository.existsByMemberAndSchedule(member, schedule);
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAll() {
        return waitingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Waiting getScheduleByWaitingId(Long id) {
        return waitingRepository.findById(id).orElseThrow(() -> new BadRequestException("존재하지 않는 일정입니다."));
    }

    @Transactional(readOnly = true)
    public boolean existsBySchedule(Schedule schedule) {
        return waitingRepository.existsBySchedule(schedule);
    }

    @Transactional(readOnly = true)
    public Waiting getFirstWaitingBySchedule(Schedule schedule) {
        return waitingRepository.findFirstWaiting(schedule).orElseThrow(() -> new BadRequestException("대기를 찾을 수 없습니다."));
    }
}
