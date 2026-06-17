package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByIdAndStatusNot(Long id, ReservationStatus status);

    @Query("""
        select count(reservation)
        from Reservation reservation
        where reservation.reservationSlot.id = :reservationSlotId
          and reservation.status <> :excludedStatus
        """)
    Long countActiveByReservationSlotId(
        @Param("reservationSlotId") Long reservationSlotId,
        @Param("excludedStatus") ReservationStatus excludedStatus
    );

    boolean existsByUserIdAndReservationSlotIdAndStatusNot(
        Long userId,
        Long reservationSlotId,
        ReservationStatus status
    );

    @Query("""
        select reservation
        from Reservation reservation
        where reservation.reservationSlot.id = :reservationSlotId
          and reservation.status <> :excludedStatus
        order by reservation.updatedAt, reservation.id
        """)
    List<Reservation> findActiveReservationsInWaitingOrder(
        @Param("reservationSlotId") Long reservationSlotId,
        @Param("excludedStatus") ReservationStatus excludedStatus
    );
}
