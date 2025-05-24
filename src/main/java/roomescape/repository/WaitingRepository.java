package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {

    @Query("""
            SELECT new roomescape.domain.reservation.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                 FROM Waiting w2
                 WHERE w2.schedule.reservationDate = w.schedule.reservationDate
                   AND w2.schedule.reservationTime = w.schedule.reservationTime
                   AND w2.schedule.theme = w.schedule.theme
                   AND w2.waitStartAt < w.waitStartAt)
            )
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(final Long memberId);
}
