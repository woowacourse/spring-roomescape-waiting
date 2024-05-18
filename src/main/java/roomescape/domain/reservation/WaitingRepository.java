package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.dto.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            select new roomescape.domain.reservation.dto.WaitingWithRank(
                w,
                (select (count(w2))
                from Waiting w2
                where w2.reservation = w.reservation and w2.id <= w.id)
            )
            from Waiting w
            where w.member = :member and w.reservation.slot.date >= :date
            """)
    List<WaitingWithRank> findWaitingRankByMember(Member member, LocalDate date);

    @Query("select w.member.id from Waiting w where w.id = :id")
    Long findMemberIdById(Long id);

    boolean existsByReservationAndMember(Reservation reservation, Member member);
}
