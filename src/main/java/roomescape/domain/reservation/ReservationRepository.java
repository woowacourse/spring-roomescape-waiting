package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.exception.NotFoundException;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    default Reservation getById(final long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다. id : " + id));
    }
}
