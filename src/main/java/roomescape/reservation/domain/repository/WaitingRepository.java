package roomescape.reservation.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationInfo;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

@Repository
public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
    List<Waiting> findByMemberId(Long memberId);

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                 FROM Waiting w2
                 WHERE w2.reservationInfo.theme = w.reservationInfo.theme
                   AND w2.reservationInfo.date = w.reservationInfo.date
                   AND w2.reservationInfo.reservationTime = w.reservationInfo.reservationTime
                   AND w2.id < w.id)+1)
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstByReservationInfoOrderByIdAsc(
            ReservationInfo reservationInfo
    );
}
