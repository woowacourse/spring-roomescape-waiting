package roomescape.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.business.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
}
