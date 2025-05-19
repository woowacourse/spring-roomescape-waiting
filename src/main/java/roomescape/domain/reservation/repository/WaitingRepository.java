package roomescape.domain.reservation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
