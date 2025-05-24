package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.BaseRepository;
import roomescape.exception.NotFoundException;

public interface ReservationRepository extends BaseRepository<Reservation, Long> {

    ReservationQueues findQueuesBySlots(List<ReservationSlot> slots);

    Reservations findAllWithWrapping(Specification<Reservation> spec);

    @Override
    Reservation save(Reservation entity);

    @Override
    Optional<Reservation> findById(Long aLong);

    @Override
    Reservation getById(Long aLong) throws NotFoundException;

    @Override
    List<Reservation> findAll(Specification<Reservation> specification);

    @Override
    boolean exists(Specification<Reservation> spec);

    @Override
    void delete(Reservation entity);

    @Override
    void deleteByIdOrElseThrow(Long aLong) throws NotFoundException;
}
