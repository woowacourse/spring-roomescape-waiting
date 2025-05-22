package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.domain.Waiting;

import java.util.List;

public interface JpaWaitingRepository extends CrudRepository<Waiting, Long> {

    boolean existsByReservationIdAndMemberId(Long reservationId, Long memberId);

    @Query("""
        SELECT new roomescape.reservation.domain.WaitingWithRank(
            w,
            (SELECT COUNT(w2) + 1
             FROM Waiting w2
             WHERE w2.reservation = w.reservation
                 AND w2.id < w.id))
        FROM Waiting w
        WHERE w.member.id = :memberId
        """
    )
    List<WaitingWithRank> findByMemberId(@Param("memberId") Long memberId);
}
