package roomescape.domain.reservation;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.exception.InvalidReservationException;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    default ReservationTime getById(long id) {
        return findById(id)
                .orElseThrow(() -> new InvalidReservationException("더이상 존재하지 않는 시간입니다."));
    }

    boolean existsByStartAt(LocalTime startAt);
}
