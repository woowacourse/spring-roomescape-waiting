package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Time;

public interface JpaTimeRepository extends JpaRepository<Time, Long>, TimeRepository {
}
