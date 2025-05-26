package roomescape.reservationtime;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    Boolean existsByStartAt(LocalTime startAt);
}
