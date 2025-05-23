package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    List<Waiting> findAll();

    Optional<Waiting> findById(Long id);

    Optional<Waiting> findByIdAndMemberId(Long id, Long memberId);

    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);

    void deleteById(Long id);
}
