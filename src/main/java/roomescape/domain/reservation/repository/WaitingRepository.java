package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
                (SELECT COUNT(w2)
                 FROM Waiting w2
                 WHERE w2.theme = w.theme
                   AND w2.date = w.date
                   AND w2.time = w.time
                   AND w2.id < w.id))
                 FROM Waiting w
                 WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    @Query("SELECT w FROM Waiting w")
    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Waiting> findAllWithMemberAndThemeAndTime();

    @Query("SELECT w FROM Waiting w WHERE w.id = :id")
    @EntityGraph(attributePaths = {"theme", "time"})
    Optional<Waiting> findByIdWithThemeAndTime(Long id);

    @Query("""
            SELECT COUNT(w)
            FROM Waiting w
            WHERE w.theme.id = :themeId
              AND w.time.id = :timeId
              AND w.date = :date
              AND w.id < :waitingId
            """)
    int countWaitingsBeforeId(@Param("date") LocalDate date,
                              @Param("timeId") Long timeId,
                              @Param("themeId") Long themeId,
                              @Param("waitingId") Long waitingId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(@Param("date") LocalDate date,
                                                      @Param("timeId") Long timeId,
                                                      @Param("themeId") Long themeId,
                                                      @Param("memberId") Long memberId);
}
