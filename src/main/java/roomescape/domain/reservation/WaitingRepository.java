package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.dto.WaitingReadOnly;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            select new roomescape.domain.reservation.dto.WaitingReadOnly(
            w.id,
            w.member,
            w.reservation.slot
            )
            from Waiting w""")
    List<WaitingReadOnly> findAllReadOnly();

    @Query("""
        select new roomescape.domain.reservation.WaitingRank(w, count(1))
        from Waiting w
        inner join Waiting w2
        on w.member = :member
        and w.reservation.slot.date >= :date
        and w.reservation = w2.reservation
        where w2.id <= w.id
        group by w.reservation
    """)
    List<WaitingRank> findRankByMemberAndDateGreaterThanEqual(Member member, LocalDate date);


}
