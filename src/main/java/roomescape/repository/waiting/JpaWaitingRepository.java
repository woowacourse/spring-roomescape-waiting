package roomescape.repository.waiting;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.waiting.Waiting;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

}
