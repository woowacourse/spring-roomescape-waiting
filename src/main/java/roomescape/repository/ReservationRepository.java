package roomescape.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import java.util.List;

@Repository
public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    List<Reservation> findByMember_Id(final Long memberId);
}
