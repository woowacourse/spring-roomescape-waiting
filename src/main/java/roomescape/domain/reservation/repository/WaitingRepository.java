package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT new roomescape.domain.reservation.WaitingWithRank(
                w,
                (SELECT COUNT(w2) AS rank
                 FROM Waiting w2
                 WHERE w2.theme = w.theme
                   AND w2.date = w.date
                   AND w2.time = w.time
                   AND w2.createdAt < w.createdAt))
                 FROM Waiting w
                 WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    @Query("SELECT w FROM Waiting w")
    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Waiting> findAllWithMemberAndThemeAndTime();

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(@Param("date") LocalDate date,
                                                       @Param("timeId") Long timeId,
                                                       @Param("themeId") Long themeId,
                                                       @Param("memberId") Long memberId);

    @Query("""
            SELECT w
            FROM Waiting w
            WHERE w.date = :date
              AND w.time.id = :timeId
              AND w.theme.id = :themeId
            ORDER BY w.createdAt ASC
            """)
    List<Waiting> findAllByDateAndTimeIdAndThemeIdOrderByCreatedAtAsc(@Param("date") LocalDate date,
                                                                      @Param("timeId") Long timeId,
                                                                      @Param("themeId") Long themeId);
}
