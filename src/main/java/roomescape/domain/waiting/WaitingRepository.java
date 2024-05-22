package roomescape.domain.waiting;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.repository.ListCrudRepository;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
    boolean existsByReservationIdAndMemberId(long reservationId, long memberId);

    List<Waiting> findByReservationIdOrderByCreatedAtAsc(long reservationId);

    default Waiting getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약 대기입니다."));
    }

    List<Waiting> findAllByMemberIdOrderByCreatedAtAsc(long memberId);
}
