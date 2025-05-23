package roomescape.waiting.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;

@Repository
public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {
}
