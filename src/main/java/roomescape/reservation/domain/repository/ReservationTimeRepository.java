package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.NotFoundException;
import roomescape.reservation.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    default ReservationTime getById(final Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorType.RESERVATION_TIME_NOT_FOUND,
                        String.format("예약 시간(ReservationTime) 정보가 존재하지 않습니다. [reservationTimeId: %d]", id)));
    }

    List<ReservationTime> findByStartAt(LocalTime startAt);
}
