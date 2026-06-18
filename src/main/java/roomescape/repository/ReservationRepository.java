package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllBySlot(Slot slot);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.slot s
            JOIN FETCH s.time
            JOIN FETCH s.theme
            """)
    List<Reservation> findAll();

    Optional<Reservation> findFirstBySlotAndStatusOrderByCreatedAtAscIdAsc(Slot slot, Status status);
}
