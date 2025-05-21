package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    void deleteById(Long id);
}
