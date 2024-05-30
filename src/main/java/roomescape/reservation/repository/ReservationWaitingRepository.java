package roomescape.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.reservation.domain.ReservationWaiting;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {

    List<ReservationWaiting> findAllByMember_IdOrderByDetailDateAsc(Long memberId);

    int countByCreateAtBeforeAndAndDetail_id(LocalDateTime startAt, Long detailId);

    Optional<ReservationWaiting> findByMember_IdAndDetail_Id(Long memberId, Long detailId);

    Optional<ReservationWaiting> findFirstByDetail_IdOrderByCreateAtDesc(Long detailId);

    List<ReservationWaiting> findAllByOrderById();

    Optional<ReservationWaiting> findByDetail_IdAndMember_Id(Long aLong, Long aLong1);
}
