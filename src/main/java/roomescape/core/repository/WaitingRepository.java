package roomescape.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.core.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
}
