package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.ReservationWait;

public interface ReservationWaitRepository extends JpaRepository<ReservationWait, Long> {

    List<ReservationWait> findAllByMember_id(Long memberId);
}
