package roomescape.reservation.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.WaitingRepository;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long>, WaitingRepository {
}
