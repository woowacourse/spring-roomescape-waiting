package roomescape.reservation.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {

    boolean existsByReservationAndMember(Reservation reservation, Member member);

    Optional<Waiting> findFirstByReservationOrderByIdAsc(Reservation reservation);

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingWithRank(
                w1,
                COUNT(w2) + 1
            )
            FROM Waiting w1
            LEFT JOIN Waiting w2
              ON w1.reservation = w2.reservation
             AND w2.id < w1.id
            WHERE w1.member.id = :memberId
            GROUP BY w1
            """)
    Page<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId, Pageable pageable);
}
