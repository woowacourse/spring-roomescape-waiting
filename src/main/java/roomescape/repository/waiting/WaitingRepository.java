package roomescape.repository.waiting;

import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    long save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeAndThemeAndMember(Waiting waiting);
}
