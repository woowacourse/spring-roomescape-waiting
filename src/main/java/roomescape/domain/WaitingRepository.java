package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    void deleteById(Long id);

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId);

    List<Waiting> findAll();

    boolean existsById(Long waitingId);

    List<Waiting> findByThemeIdAndDateAndTimeId(Long themeId, LocalDate date, Long timeId);
}
