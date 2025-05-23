package roomescape.waiting.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long>, WaitingRepository {
}
