package roomescape.repository.reservationwaiting.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationWaitingJpaRepository extends JpaRepository<ReservationWaitingJpaEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})
    Optional<ReservationWaitingJpaEntity> findById(Long id);

    @EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})
    List<ReservationWaitingJpaEntity> findAllBySlot_IdOrderByRequestedAtAscIdAsc(Long slotId);
}
