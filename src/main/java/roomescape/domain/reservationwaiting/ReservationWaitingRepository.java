package roomescape.domain.reservationwaiting;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {
    List<ReservationWaiting> findAllByReservation(Reservation reservation);

    @Query("""
             SELECT new roomescape.domain.reservationwaiting.WaitingWithRank(
                 w,
                (SELECT COUNT(w1) + 1
                 FROM ReservationWaiting w1
                 WHERE w1.reservation = w.reservation
                   AND w1.createdAt < w.createdAt)
                   )
            FROM ReservationWaiting w
            WHERE w.member = :member
            """)
    List<WaitingWithRank> findAllWithRankByMember(Member member);
}
