package roomescape.reservation.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByReservationSlotAndMember(ReservationSlot reservationSlot, Member member);

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingWithRank(
            	w,
            	(SELECT COUNT(w2)
            		FROM Waiting w2
            		WHERE w2.reservationSlot.theme = w.reservationSlot.theme
            		AND w2.reservationSlot.date = w.reservationSlot.date
            		AND w2.reservationSlot.time = w.reservationSlot.time
            		AND w2.createdAt <= w.createdAt)
            	)
            FROM Waiting w
            WHERE w.member.id = :memberId
            """
    )
    List<WaitingWithRank> findAllWaitingWithRankByMemberId(Long memberId);

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                    FROM Waiting w2
                    WHERE w2.reservationSlot.theme = w.reservationSlot.theme
                    AND w2.reservationSlot.date = w.reservationSlot.date
                    AND w2.reservationSlot.time = w.reservationSlot.time
                    AND w2.createdAt <= w.createdAt)
                )
            FROM Waiting w
            """
    )
    List<WaitingWithRank> findAllWaitingWithRank();

    Optional<Waiting> findFirstByReservationSlotOrderByCreatedAt(ReservationSlot reservationSlot);
}
