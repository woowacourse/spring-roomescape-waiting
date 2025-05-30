package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, long timeId, long themeId, long memberId);

    @Query("""

            SELECT new roomescape.waiting.domain.WaitingWithRank(
            w,
            CAST((SELECT COUNT(w2)
                  FROM Waiting w2
                  WHERE w2.date = w.date
                    AND w2.theme = w.theme
                    AND w2.time = w.time
                    AND w2.id <= w.id) AS long))
        FROM Waiting w
        WHERE w.member.id = :memberId
        """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(@Param("memberId") long memberId);

    Optional<Waiting> findTopByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
}
