package roomescape.repository.reservationtime;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.ReservationTime;

public interface ReservationTimeRepositoryImpl extends JpaRepository<ReservationTime, Long>, ReservationTimeRepository {

}
