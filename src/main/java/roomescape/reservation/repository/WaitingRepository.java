package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.reservation.domain.ReservationStatus;

public interface WaitingRepository extends JpaRepository<ReservationStatus, Long> {

}
