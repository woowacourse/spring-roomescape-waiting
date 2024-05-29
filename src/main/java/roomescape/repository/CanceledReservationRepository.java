package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.entity.CanceledReservation;

public interface CanceledReservationRepository extends JpaRepository<CanceledReservation, Long> {
}
