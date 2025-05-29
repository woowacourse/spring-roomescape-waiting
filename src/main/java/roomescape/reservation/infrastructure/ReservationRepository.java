package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.reservationSlot rs
            JOIN FETCH rs.time t
            JOIN FETCH rs.theme th
            JOIN FETCH r.member m
            WHERE (:themeId IS NULL OR th.id = :themeId)
              AND (:memberId IS NULL OR m.id = :memberId)
              AND (:startDate IS NULL OR rs.date >= :startDate)
              AND (:endDate IS NULL OR rs.date <= :endDate)
              AND r.id IN (
                SELECT MIN(r2.id)
                FROM Reservation r2
                GROUP BY r2.reservationSlot.id
              )             
            ORDER BY rs.id, r.createdAt asc 
            """)
    List<Reservation> findFirstByCriteria(Long themeId, LocalDate startDate,
                                          LocalDate endDate, Long memberId);

    @Query("""
            SELECT r 
            FROM Reservation r 
            JOIN FETCH r.reservationSlot rs            
            JOIN FETCH rs.time t 
            JOIN FETCH rs.theme th                   
            WHERE r.member.id = :memberId
            """)
    List<Reservation> findByMemberId(Long memberId);

    void deleteByReservationSlotIdAndMemberId(Long reservationSlotId, Long memberId);

    boolean existsByReservationSlotIdAndMemberId(Long reservationSlotId, Long memberId);

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
