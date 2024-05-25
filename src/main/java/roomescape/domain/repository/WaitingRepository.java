package roomescape.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
}
