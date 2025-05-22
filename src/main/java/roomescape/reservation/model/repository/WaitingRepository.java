package roomescape.reservation.model.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.vo.WaitingWithRank;

public interface WaitingRepository {

    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    Waiting save(Waiting waiting);

    List<Waiting> findAllByMemberId(Long memberId);

    void delete(Waiting waiting);

    Optional<Waiting> findById(Long id);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
