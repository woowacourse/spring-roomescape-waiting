package roomescape.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.business.domain.WaitInfo;

public interface WaitInfoRepository extends JpaRepository<WaitInfo, Long> {
}
