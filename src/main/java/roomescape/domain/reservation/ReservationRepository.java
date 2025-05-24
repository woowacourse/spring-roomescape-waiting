package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.BaseRepository;
import roomescape.exception.NotFoundException;

public interface ReservationRepository extends BaseRepository<Reservation, Long> {

    @Override
    Reservation save(Reservation entity);

    @Override
    Optional<Reservation> findById(Long id);

    @Override
    Reservation getById(Long id) throws NotFoundException;

    ReservationQueues findQueuesBySlots(List<ReservationSlot> slots);

    Reservations findAllWithWrapping(Specification<Reservation> spec);

    @Override
    List<Reservation> findAll(Specification<Reservation> specification);

    @Override
    boolean exists(Specification<Reservation> spec);

    @Override
    void delete(Reservation entity);

    @Override
    void deleteByIdOrElseThrow(Long id) throws NotFoundException;
}
