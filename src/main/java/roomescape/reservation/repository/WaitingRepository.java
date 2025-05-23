package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
