package roomescape.waiting.domain;

import java.util.Collection;
import java.util.Optional;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    void deleteById(Long id);

    Collection<Waiting> findAll();

    Collection<WaitingWithRank> findWithRankByMemberId(Long memberId);

    Optional<WaitingWithRank> findWithRankById(Long id);
}
