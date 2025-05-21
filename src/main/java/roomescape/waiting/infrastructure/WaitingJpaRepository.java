package roomescape.waiting.infrastructure;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long> {
    @Query("""
            SELECT w FROM Waiting w
            JOIN FETCH w.member
            JOIN FETCH w.spec.theme
            JOIN FETCH w.spec.time
            """)
    Collection<Waiting> findAllWithEagerLoading();

    @Query("""
            SELECT new roomescape.waiting.domain.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                 FROM Waiting w2
                 WHERE w2.spec.theme = w.spec.theme
                   AND w2.spec.date = w.spec.date
                   AND w2.spec.time = w.spec.time
                   AND w2.createdAt < w.createdAt) + 1)
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    Collection<WaitingWithRank> findWithRankByMemberId(Long memberId);

    @Query("""
            SELECT new roomescape.waiting.domain.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                 FROM Waiting w2
                 WHERE w2.spec.theme = w.spec.theme
                   AND w2.spec.date = w.spec.date
                   AND w2.spec.time = w.spec.time
                   AND w2.createdAt < w.createdAt) + 1)
            FROM Waiting w
            WHERE w.id = :id
            """)
    Optional<WaitingWithRank> findWithRankById(Long id);
}
