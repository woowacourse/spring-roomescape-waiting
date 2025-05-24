package roomescape.infrastructure;

import java.util.List;
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
    default Reservation getById(final Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다. id : " + id));
    }

    @Override
    default Reservations findAllWithWrapping(final Specification<Reservation> specification) {
        var reservations = findAll(specification);
        return new Reservations(reservations);
    }

    @Override
    default ReservationQueues findQueuesBySlots(final List<ReservationSlot> slots) {
        var reservations = findAll(toSpecs(slots));
        return new ReservationQueues(reservations);
    }

    @Modifying
    @Query("DELETE FROM RESERVATION r WHERE r.id = :id")
    @Transactional
    int deleteByIdAndCount(final Long id);

    @Transactional
    default void deleteByIdOrElseThrow(final Long id) {
        var deletedCount = deleteByIdAndCount(id);
        if (deletedCount == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다. id : " + id);
        }
    }

    private Specification<Reservation> toSpecs(final List<ReservationSlot> slots) {
        return Specification.anyOf(slots.stream().map(ReservationSpecs::bySlot).toList());
    }
}
