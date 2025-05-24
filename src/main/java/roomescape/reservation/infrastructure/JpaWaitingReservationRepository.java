package roomescape.reservation.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.domain.dto.WaitingReservationWithRank;

public interface JpaWaitingReservationRepository extends CrudRepository<WaitingReservation, Long> {

    @Query("""
        SELECT new roomescape.reservation.domain.dto.WaitingReservationWithRank(
            w,
            (SELECT COUNT(w2)
                FROM WaitingReservation w2
                WHERE w2.theme = w.theme
                    AND w2.date = w.date
                    AND w2.time = w.time
                    AND w2.createdAt < w.createdAt))
        FROM WaitingReservation w
        WHERE w.member.id = :memberId
        """)
    List<WaitingReservationWithRank> findWaitingsWithRankByMember_Id(Long memberId);
}
