package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Waiting;
import roomescape.persistence.dto.WaitingWithRankData;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(Long memberId, Long themeId, Long timeId, LocalDate date);

    @Query("""
        SELECT w FROM Waiting w
        WHERE w.theme.id = :themeId
          AND w.time.id = :timeId
          AND w.date = :date
        ORDER BY w.id ASC
        LIMIT 1
    """)
    Optional<Waiting> findFirstWaiting(LocalDate date, Long themeId, Long timeId);

    @Query("""
        SELECT w FROM Waiting w
        JOIN FETCH w.member
        JOIN FETCH w.time
        JOIN FETCH w.theme
        WHERE w.member.id = :memberId
        ORDER BY w.id
    """)
    List<Waiting> findByMemberId(Long memberId);

    @Query("""
        SELECT new roomescape.persistence.dto.WaitingWithRankData(
            w,
            (
                SELECT COUNT(w2)
                FROM Waiting w2
                WHERE w2.date = w.date
                  AND w2.time = w.time
                  AND w2.theme = w.theme
                  AND w2.waitingStartedAt < w.waitingStartedAt
            )
        )
        FROM Waiting w
        WHERE w.member.id = :memberId
        ORDER BY w.waitingStartedAt, w.id
    """)
    List<WaitingWithRankData> findWaitingsWithRankByMemberId(Long memberId);

    @Query("""
        SELECT w FROM Waiting w
        JOIN FETCH w.member
        JOIN FETCH w.time
        JOIN FETCH w.theme
        ORDER BY w.id ASC
    """)
    List<Waiting> findAllWaitings();
}
