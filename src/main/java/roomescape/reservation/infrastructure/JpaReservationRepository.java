package roomescape.reservation.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public interface JpaReservationRepository extends ListCrudRepository<Reservation, Long> {

    @Query("""
            SELECT w 
            FROM Reservation w 
            JOIN FETCH w.bookingSlot r            
            JOIN FETCH r.time t 
            JOIN FETCH r.theme th                   
            WHERE w.member.id = :memberId
            """)
    List<Reservation> findByReservationMemberId(Long memberId);

    @Modifying
    @Query("""
            DELETE FROM Reservation w 
            WHERE w.bookingSlot.id = :reservationId
            AND w.member.id = :memberId
            """)
    void deleteByBookingSlotIdAndMemberId(Long reservationId, Long memberId);

    boolean existsByBookingSlotIdAndMemberId(Long reservationId, Long memberId);

    @Query("""
            SELECT w 
            FROM Reservation w 
            JOIN FETCH w.bookingSlot r            
            JOIN FETCH r.time t 
            JOIN FETCH r.theme th                   
            WHERE w.reservationStatus = :reservationStatus
            """)
    List<Reservation> findAllByReservationStatus(ReservationStatus reservationStatus);
}
