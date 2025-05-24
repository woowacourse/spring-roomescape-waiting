package roomescape.waiting.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

import java.util.List;

@Repository
public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {
    @Query("""
            SELECT new roomescape.waiting.domain.WaitingWithRank(
                            w, 
                            (SELECT COUNT(w2) 
                             FROM Waiting w2 
                             WHERE w2.schedule.theme = w.schedule.theme 
                               AND w2.schedule.date = w.schedule.date 
                               AND w2.schedule.time = w.schedule.time
                               AND w2.id < w.id)) 
                        FROM Waiting w 
                        WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

}
