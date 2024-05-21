package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationWait;

public interface ReservationWaitRepository extends JpaRepository<ReservationWait, Long> {
    List<ReservationWait> findByMemberId(long memberId);
}
