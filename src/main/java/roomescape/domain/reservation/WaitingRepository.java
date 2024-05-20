package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.dto.WaitingReadOnly;
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
            join fetch w.reservation r
            join fetch r.slot s
            join fetch s.time
            join fetch s.theme
            where w.member = :member and w.reservation.slot.date >= :date
            """)
    List<WaitingWithRank> findWaitingRankByMemberAndDateAfter(Member member, LocalDate date);

    @Query("select w.member.id from Waiting w where w.id = :id")
    Long findMemberIdById(Long id);

    @Query("""
            select new roomescape.domain.reservation.dto.WaitingReadOnly(
            w.id,
            w.member,
            w.reservation.slot
            )
            from Waiting w""")
    List<WaitingReadOnly> findAllReadOnly();

    Optional<Waiting> findFirstByReservation(Reservation reservation, Sort sort);
}
