package roomescape.waiting.repository;

import java.util.List;
import java.util.Optional;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public interface WaitingRepository {

    List<Waiting> findAll();

    Waiting save(Waiting waiting);

    List<WaitingWithRank> findAllWaitingWithRankByMemberId(Long memberId);

    Optional<Waiting> findById(Long id);

    void deleteById(Long id);

    List<WaitingWithRank> findAllWithRank();
}
