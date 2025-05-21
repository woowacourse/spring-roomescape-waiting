package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.reservation.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

}
