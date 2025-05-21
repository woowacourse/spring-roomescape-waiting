package roomescape.application.reservation.query;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.query.dto.WaitingResult;
import roomescape.application.reservation.query.dto.WaitingWithRankResult;
import roomescape.domain.reservation.WaitingWithRank;
import roomescape.domain.reservation.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingQueryService {

    private final WaitingRepository waitingRepository;

    public WaitingQueryService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public List<WaitingWithRankResult> findWaitingByMemberId(Long memberId) {
        List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(memberId);
        return waitings.stream()
                .map(w -> WaitingWithRankResult.from(w.waiting(), oneBasedRank(w.rank())))
                .toList();
    }

    private long oneBasedRank(long zeroBasedRank) {
        return zeroBasedRank + 1;
    }

    public List<WaitingResult> findAll() {
        return waitingRepository.findAllWithMemberAndThemeAndTime()
                .stream()
                .map(WaitingResult::from)
                .toList();
    }
}
