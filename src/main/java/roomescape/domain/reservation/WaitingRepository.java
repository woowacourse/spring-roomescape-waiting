package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.member.Member;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByReservationSlotAndMember(@Param("reservationSlot") ReservationSlot reservationSlot,
                                             @Param("member") Member member);

    @Query("""
                SELECT new roomescape.domain.reservation.WaitingRank(w, (
                    SELECT COUNT(w2) + 1
                    FROM Waiting w2
                    WHERE w2.reservationSlot = w.reservationSlot
                      AND w2.startedAt < w.startedAt))
                FROM Waiting w
                WHERE w.member = :member
            """)
    List<WaitingRank> findWaitingRankByMember(@Param("member") Member member);

    @Query("""
                SELECT w
                FROM Waiting w
                    JOIN fetch w.member
                    JOIN fetch w.reservationSlot.theme
                    JOIN fetch w.reservationSlot.time
            """)
    List<Waiting> findAllWithMemberAndThemeAndTime();

    Optional<Waiting> findTopByReservationSlotOrderByStartedAtAsc(
            @Param("reservationSlot") ReservationSlot reservationSlot);
}
