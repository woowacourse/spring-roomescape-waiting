package roomescape.waiting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.waiting.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
}
