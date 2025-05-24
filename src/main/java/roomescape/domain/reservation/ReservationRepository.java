package roomescape.domain.reservation;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.NotFoundException;
import roomescape.infrastructure.ReservationSpecs;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    @Modifying
    @Query("DELETE FROM Reservation r WHERE r.id = :id")
    @Transactional
    int deleteByIdAndCount(@Param("id") long id);

    @Transactional
    default void deleteByIdOrElseThrow(final long id) {
        var deletedCount = deleteByIdAndCount(id);
        if (deletedCount == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다. id : " + id);
        }
    }

    @Transactional(readOnly = true)
    default ReservationQueues findQueuesBySlots(final List<ReservationSlot> slots) {
        var reservations = findAll(toSpecs(slots));
        return new ReservationQueues(reservations);
    }

    private Specification<Reservation> toSpecs(final List<ReservationSlot> slots) {
        return Specification.anyOf(slots.stream().map(ReservationSpecs::bySlot).toList());
    }

    default Reservation getById(final long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다. id : " + id));
    }
}
