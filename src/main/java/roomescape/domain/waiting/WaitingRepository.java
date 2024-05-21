package roomescape.domain.waiting;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.role.MemberRole;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
    boolean existsByReservationIdAndMemberId(long reservationId, long memberId);

    default Waiting getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약 대기입니다."));
    }
}
