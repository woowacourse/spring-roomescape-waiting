package roomescape.domain.waiting;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
    boolean existsByReservationIdAndMemberId(long reservationId, long memberId);

    List<Waiting> findByReservationIdOrderByCreatedAtAsc(long reservationId);

    @Query("SELECT w FROM Waiting w WHERE w.reservation.id = :reservationId ORDER BY w.createdAt ASC LIMIT 1")
    Optional<Waiting> findFirstByReservationIdOrderByCreatedAtAsc(long reservationId);

    default Waiting getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약 대기입니다."));
    }

    List<Waiting> findAllByMemberIdOrderByCreatedAtAsc(long memberId);
}
