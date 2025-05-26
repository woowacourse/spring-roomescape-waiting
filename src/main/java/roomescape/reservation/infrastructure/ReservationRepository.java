package roomescape.reservation.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {

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
            SELECT w 
            FROM Reservation w 
            JOIN FETCH w.reservationSlot r            
            JOIN FETCH r.time t 
            JOIN FETCH r.theme th                   
            WHERE w.reservationStatus = :reservationStatus
            """)
    List<Reservation> findAllByReservationStatus(ReservationStatus reservationStatus);
}
