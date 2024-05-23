package roomescape.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationWaiting;

@Repository
public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {
    List<ReservationWaiting> findAllByMemberId(Long memberId);

    List<ReservationWaiting> findAll();

    boolean existsByReservationIdAndMemberId(Long reservationId, Long memberId);

    long countByReservationId(Long reservationId);

    @Query("""
            SELECT COUNT(w.id)
            FROM ReservationWaiting AS w
            WHERE w.reservation.id = :reservationId
            AND w.priority <= :standard
            """)
    long getRankByReservationAndPriority(
            @Param("reservationId") Long reservationId,
            @Param("standard") Long priority);
}
