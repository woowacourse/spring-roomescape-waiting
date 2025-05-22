package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.WaitingReservation;

public interface JpaWaitingReservationRepository extends JpaRepository<WaitingReservation, Long> {

    @Query("SELECT COALESCE(MAX(w.waitingOrder), 0) FROM WaitingReservation w WHERE w.reservation.id = :reservationId")
    Integer findMaxWaitingOrderByReservationId(@Param("reservationId") Long reservationId);
}

