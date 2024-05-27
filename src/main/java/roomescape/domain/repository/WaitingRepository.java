package roomescape.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;

import java.time.LocalDate;
import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    boolean existsByMemberAndReservation(Member member, Reservation reservation);

    @Query("select exists(select 1 from Waiting w where w=:waiting and w.member = :member)")
    boolean existsByWaitingAndMember(Waiting waiting, Member member);

    @Query("""
            select new roomescape.domain.reservation.WaitingWithRank(w,
              (select count(w2)
               from Waiting w2
               where w2.reservation = w.reservation
                 and w2.createAt < w.createAt))
            from Waiting w
            join fetch w.reservation reservation
            join fetch w.member
            where w.member = :member
                and reservation.reservationSlot.date >= :date""")
    List<WaitingWithRank> findMemberWaitingWithRankAndDateGreaterThanEqual(Member member, LocalDate date);

    List<Waiting> findByReservationOrderById(Reservation reservation);
}
