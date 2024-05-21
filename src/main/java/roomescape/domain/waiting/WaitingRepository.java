package roomescape.domain.waiting;

import java.time.LocalDate;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.role.MemberRole;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
    boolean existsByReservationIdAndMemberId(long reservationId, long memberId);
}
