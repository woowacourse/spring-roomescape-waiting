package roomescape.reservation.infrastructure;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    default ReservationTime getByIdOrThrow(Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 시간이 존재하지 않습니다."));
    }

    List<ReservationTime> findAllByStartAt(LocalTime startAt);
}
