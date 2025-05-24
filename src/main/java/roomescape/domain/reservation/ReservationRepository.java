package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.BaseRepository;
import roomescape.exception.NotFoundException;

public interface ReservationRepository extends BaseRepository<Reservation, Long> {

    @Override
    Reservation save(Reservation reservation);

    @Override
    Optional<Reservation> findById(Long id);

    @Override
    Reservation getById(Long id) throws NotFoundException;

    ReservationQueues findQueuesBySlots(List<ReservationSlot> slots);

    Reservations findAllWithWrapping(Specification<Reservation> specification);

    @Override
    List<Reservation> findAll(Specification<Reservation> specification);

    @Override
    boolean exists(Specification<Reservation> specification);

    @Override
    void delete(Reservation reservation);

    @Override
    void deleteByIdOrElseThrow(Long id) throws NotFoundException;
}
