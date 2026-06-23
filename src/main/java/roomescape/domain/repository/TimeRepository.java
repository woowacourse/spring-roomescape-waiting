package roomescape.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Time;

public interface TimeRepository extends JpaRepository<Time,Long> {
}
