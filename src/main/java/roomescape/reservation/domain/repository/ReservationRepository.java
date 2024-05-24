package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    List<Reservation> findAllByMember(Member member);

    List<Reservation> findAllByStatus(ReservationStatus status);

    Optional<Reservation> findFirstByReservationSlotOrderByCreatedTime(ReservationSlot reservationSlot);

    @Query("""
    SELECT count(*)
    FROM Reservation  r
    WHERE r.reservationSlot = 
    (SELECT r.reservationSlot
    FROM Reservation r
    WHERE r.id = :reservationId)
    AND r.id < :reservationId
    """)
    int countWaitingReservation(Long reservationId);

    boolean existsByReservationSlot(ReservationSlot reservationSlot);

    void deleteByReservationSlot_Id(long reservationSlotId);

    boolean existsByReservationSlotAndMember(ReservationSlot reservationSlot, Member member);
}
