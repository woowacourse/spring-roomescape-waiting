package roomescape.domain;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {
    @EntityGraph(attributePaths = {"reservation"})
    List<ReservationWaiting> findAllByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"reservation"})
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

    @EntityGraph(attributePaths = {"reservation"})
    Page<ReservationWaiting> findAllByReservationIdOrderByPriorityAsc(Pageable pageable, Long reservationId);
}
