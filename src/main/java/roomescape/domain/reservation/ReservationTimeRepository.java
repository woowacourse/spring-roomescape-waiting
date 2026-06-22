package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.RoomEscapeException;

import java.util.Collection;
import java.util.List;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findByIdNotIn(Collection<Long> ids);

    default ReservationTime getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 시간을 찾을 수 없습니다. : " + id));
    }
}
