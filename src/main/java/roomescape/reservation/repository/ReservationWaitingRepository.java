package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.reservation.domain.ReservationWaiting;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {
    List<ReservationWaiting> findAllByMember_IdOrderByDetailDateAsc(Long memberId);

    Optional<ReservationWaiting> findByDetail_Id(Long id);
}
