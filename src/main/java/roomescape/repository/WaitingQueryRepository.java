package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.waiting.WaitingWithRankResponse;

@Repository
public interface WaitingQueryRepository extends JpaRepository<Waiting, Long> {

    @Query(value = """
                SELECT new roomescape.domain.waiting.WaitingWithRank(
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
    List<WaitingWithRankResponse> findWaitingsWithRankByMemberId(@Param("memberId") Long memberId);
}
