package roomescape.reservation.repository;

import java.time.LocalTime;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeId;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, ReservationTimeId> {

    boolean existsByStartAt(LocalTime startAt);

}
