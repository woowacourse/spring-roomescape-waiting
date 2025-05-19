package roomescape.waiting.domain;

import java.util.Collection;
import org.springframework.data.jpa.repository.Query;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    void deleteById(Long id);

    Collection<Waiting> findAll();

    @Query("""
                SELECT w FROM Waiting w
                JOIN FETCH w.member
                JOIN FETCH w.spec.theme
                JOIN FETCH w.spec.time
                WHERE w.member.id = :memberId
            """)
    Collection<Waiting> findAllByMemberId(Long memberId);

    @Query("""
            SELECT new roomescape.waiting.domain.WaitingWithRank(
                w,
                (SELECT COUNT(w2) FROM Waiting w2
                    WHERE w2.spec.theme = w.spec.theme
                        AND w2.spec.date = w.spec.date
                        AND w2.spec.time = w.spec.time
                        AND w2.createdAt < w.createdAt))
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    Collection<Waiting> findWithRankByMemberId(Long memberId);
}
