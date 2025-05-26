package roomescape.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Status;

public interface WaitingRepository extends JpaRepository<Status, Long> {
}
