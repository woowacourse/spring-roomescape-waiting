package roomescape.domain.repository;

import org.springframework.data.repository.Repository;
import roomescape.domain.ReservationTime;

public interface ReservationTimeCommandRepository extends Repository<ReservationTime, Long> {

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);
}
