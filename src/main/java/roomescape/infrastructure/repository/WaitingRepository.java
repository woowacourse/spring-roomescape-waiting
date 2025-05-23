package roomescape.infrastructure.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.business.domain.Waiting;
import roomescape.business.domain.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    @Query(
            "SELECT new roomescape.business.domain.WaitingWithRank(w, "
                    + "(SELECT COUNT (w2) "
                    + "FROM Waiting w2 "
                    + "WHERE w2.theme=w.theme "
                    + "AND w2.date=w.date "
                    + "AND w2.time=w.time "
                    + "AND w2.createdAt <= w.createdAt)) "
            + "FROM Waiting w "
            + "WHERE w.member.id=:memberId"
    )
    List<WaitingWithRank> findWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);

    Optional<Waiting> findFirstByDateAndThemeIdAndTimeIdOrderByCreatedAtAsc(LocalDate date, Long themeId, Long timeId);
}
