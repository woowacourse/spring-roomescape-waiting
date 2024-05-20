package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationTime;

import java.time.LocalTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);

    @Transactional
    long deleteReservationTimeById(long timeId);
}
