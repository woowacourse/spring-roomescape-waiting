package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Reservation getById(long id);

    Optional<Reservation> findById(long id);

    ReservationQueues findQueuesBySlots(List<ReservationSlot> slots);

    List<Reservation> findAll(Specification<Reservation> spec);

    Reservations findAllWithWrapping(Specification<Reservation> spec);

    boolean exists(Specification<Reservation> spec);

    void delete(Reservation Reservation);

    void deleteByIdOrElseThrow(long id);
}
