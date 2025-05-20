package roomescape.application.reservation.query;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.query.dto.WaitingResult;
import roomescape.application.reservation.query.dto.WaitingWithRankResult;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.WaitingWithRank;
import roomescape.domain.reservation.repository.WaitingRepository;
import roomescape.infrastructure.error.exception.ReservationException;

@Service
@Transactional(readOnly = true)
public class WaitingQueryService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;

    public WaitingQueryService(WaitingRepository waitingRepository, MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
    }

    public List<WaitingWithRankResult> findWaitingByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ReservationException("회원 정보가 존재하지 않습니다."));
        List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(member.getId());
        return waitings.stream()
                .map(waitingWithRank -> new WaitingWithRank(waitingWithRank.waiting(), waitingWithRank.rank() + 1))
                .map(waitingWithRank -> WaitingWithRankResult.from(waitingWithRank.waiting(), waitingWithRank.rank()))
                .toList();
    }

    public List<WaitingResult> findAll() {
        return waitingRepository.findAllWithMemberAndThemeAndTime()
                .stream()
                .map(WaitingResult::from)
                .toList();
    }
}
