package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;

import java.time.LocalDate;
import java.util.List;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
    SELECT exists (
        SELECT w
        FROM Waiting w
        WHERE w.date = :date AND w.time.id = :timeId AND w.theme.id = :themeId AND w.member.id = :memberId)
    """)
    boolean existsFor(LocalDate date, Long timeId, Long themeId, Long memberId);

    @Query("""
    SELECT new roomescape.domain.waiting.WaitingWithRank(
        w,
        (SELECT COUNT(w2) + 1
         FROM Waiting w2
         WHERE w2.theme = w.theme
           AND w2.date = w.date
           AND w2.time = w.time
           AND w2.id < w.id)
    )
    FROM Waiting w
    WHERE w.member.id = :memberId
""")
    List<WaitingWithRank> findByMemberId(long memberId);
}
