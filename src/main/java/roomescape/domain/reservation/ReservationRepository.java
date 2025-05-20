package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.exception.NotFoundException;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    @Modifying
    @Query("DELETE FROM Reservation r WHERE r.id = :id")
    int deleteByIdAndCount(@Param("id") long id);

    default void deleteByIdOrElseThrow(final long id) {
        var deletedCount = deleteByIdAndCount(id);
        if (deletedCount == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다. id : " + id);
        }
    }

    default Reservation getById(final long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다. id : " + id));
    }
}
