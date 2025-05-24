package roomescape.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationQueues;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.Reservations;
import roomescape.exception.NotFoundException;

public interface ReservationJpaRepository extends ReservationRepository, Repository<Reservation, Long> {

    @Override
    Reservation save(Reservation reservation);

    @Override
    default Reservation getById(final long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다. id : " + id));
    }

    @Override
    boolean exists(Specification<Reservation> spec);

    @Override
    List<Reservation> findAll(Specification<Reservation> spec);

    @Override
    default Reservations findAllWithWrapping(Specification<Reservation> spec) {
        var reservations = findAll(spec);
        return new Reservations(reservations);
    }

    @Override
    Optional<Reservation> findById(long id);

    @Override
    void delete(final Reservation entity);

    @Override
    default ReservationQueues findQueuesBySlots(final List<ReservationSlot> slots)  {
        var reservations = findAll(toSpecs(slots));
        return new ReservationQueues(reservations);
    }

    @Modifying
    @Query("DELETE FROM Reservation r WHERE r.id = :id")
    @Transactional
    int deleteByIdAndCount(final long id);

    @Transactional
    default void deleteByIdOrElseThrow(final long id) {
        var deletedCount = deleteByIdAndCount(id);
        if (deletedCount == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다. id : " + id);
        }
    }

    private Specification<Reservation> toSpecs(final List<ReservationSlot> slots) {
        return Specification.anyOf(slots.stream().map(ReservationSpecs::bySlot).toList());
    }
}
