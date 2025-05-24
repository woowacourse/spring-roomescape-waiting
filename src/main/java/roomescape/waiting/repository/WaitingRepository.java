package roomescape.waiting.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.entity.ReservationSlot;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.entity.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    long countByReservationSlotAndIdLessThan(ReservationSlot reservationSlot, Long id);

    @Query("""
            SELECT new roomescape.waiting.entity.WaitingWithRank(
                           w,
                           (SELECT COUNT(w2)
                            FROM Waiting w2
                            WHERE w2.reservationSlot.theme = w.reservationSlot.theme
                              AND w2.reservationSlot.date = w.reservationSlot.date
                              AND w2.reservationSlot.time = w.reservationSlot.time
                              AND w2.id < w.id))
                       FROM Waiting w
                       WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstByReservationSlot(ReservationSlot reservationSlot);

    boolean existsByReservationSlotAndMemberId(ReservationSlot reservationSlot, Long memberId);
}
