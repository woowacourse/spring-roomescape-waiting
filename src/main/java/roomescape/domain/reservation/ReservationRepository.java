package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.RoomEscapeException;

import java.util.List;
import java.util.Optional;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByName(ReservationName name);

    List<Reservation> findBySlot_Id(Long slotId);

    default Reservation getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 예약을 찾을 수 없습니다. : " + id));
    }
}
