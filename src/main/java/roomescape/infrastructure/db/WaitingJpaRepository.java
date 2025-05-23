package roomescape.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.model.Waiting;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long> {

}
