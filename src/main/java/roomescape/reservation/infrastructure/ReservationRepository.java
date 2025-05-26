package roomescape.reservation.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT w 
            FROM Reservation w 
            JOIN FETCH w.reservationSlot r            
            JOIN FETCH r.time t 
            JOIN FETCH r.theme th                   
            WHERE w.member.id = :memberId
            """)
    List<Reservation> findByReservationMemberId(Long memberId);

    @Modifying
    @Query("""
            DELETE FROM Reservation w 
            WHERE w.reservationSlot.id = :reservationId
            AND w.member.id = :memberId
            """)
    void deleteByReservationSlotIdAndMemberId(Long reservationId, Long memberId);

    boolean existsByReservationSlotIdAndMemberId(Long reservationId, Long memberId);

    @Query("""
            SELECT r 
            FROM Reservation r 
            JOIN FETCH r.reservationSlot rs            
            JOIN FETCH rs.time t 
            JOIN FETCH rs.theme th      
            WHERE r.id NOT IN (
            SELECT MIN(r2.id)
            FROM Reservation r2
            GROUP BY r2.reservationSlot.id
            )             
            ORDER BY rs.id, r.createdAt asc 
            """)
    List<Reservation> findAllWaitingReservations();
}
