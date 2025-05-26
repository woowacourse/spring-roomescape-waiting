package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.waiting.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
}
