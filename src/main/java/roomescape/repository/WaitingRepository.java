package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT new roomescape.model.WaitingWithRank(w, (SELECT COUNT(w2) + 1
                        FROM Waiting w2
                        WHERE w2.theme = w.theme
                            AND w2.date = w.date
                            AND w2.time = w.time
                            AND w2.id < w.id))
            FROM Waiting w
            WHERE w.member.id = ?1
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);
}
