package roomescape.repository.waiting;

import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    long save(Waiting waiting);

    List<Waiting> findAll();

    Optional<Waiting> findById(Long id);

    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    List<Waiting> findByDateAndThemeId(LocalDate date, Long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeAndThemeAndMember(Waiting waiting);

    void deleteById(Long id);
}
