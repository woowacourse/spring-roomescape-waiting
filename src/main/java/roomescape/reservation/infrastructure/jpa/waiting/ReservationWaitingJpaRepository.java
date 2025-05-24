package roomescape.reservation.infrastructure.jpa.waiting;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.waiting.ReservationWaiting;
import roomescape.reservation.domain.waiting.ReservationWaitingWithRank;

public interface ReservationWaitingJpaRepository extends JpaRepository<ReservationWaiting, Long> {

    boolean existsByReservationIdAndMemberId(long reservationId, long memberId);

    @Query("""

            SELECT new roomescape.reservation.domain.waiting.ReservationWaitingWithRank(
            w,
            CAST((SELECT COUNT(w2)
                  FROM ReservationWaiting w2
                  WHERE w2.reservation.theme = w.reservation.theme
                    AND w2.reservation.date = w.reservation.date
                    AND w2.reservation.time = w.reservation.time
                    AND w2.id <= w.id) AS long))
        FROM ReservationWaiting w
        WHERE w.member.id = :memberId
        """)
    List<ReservationWaitingWithRank> findWaitingsWithRankByMemberId(@Param("memberId") long memberId);
}
