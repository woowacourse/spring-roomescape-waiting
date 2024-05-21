package roomescape.waiting.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

@Repository
public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
    List<Waiting> findByMemberId(Long memberId);
    Long countByReservationAndCreatedAtLessThanEqual(Reservation reservation, LocalDateTime dateTime);
    boolean existsByReservationIdAndMemberId(Long reservationId, Long memberId);
}
