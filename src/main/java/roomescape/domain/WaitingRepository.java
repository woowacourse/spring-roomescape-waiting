package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    public int findMaxOrderByThemeIdAndDateAndTimeId(Long themeId, LocalDate date, Long reservationTimeId);

}
