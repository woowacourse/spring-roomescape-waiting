package roomescape.reservationTime.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
}
