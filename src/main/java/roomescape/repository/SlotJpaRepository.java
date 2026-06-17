package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Slot;

@Repository
public interface SlotJpaRepository extends JpaRepository<Slot, Long> {
}
