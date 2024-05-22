package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.user.Member;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    List<Waiting> findAllByMemberId(Long memberId);
    List<Waiting> findAllByReservationInfoOrderByCreatedDateAsc(ReservationInfo reservationInfo);
    Optional<Waiting> findFirstByReservationInfoOrderByCreatedDateAsc(ReservationInfo reservationInfo);
    boolean existsByMemberAndReservationInfo(Member member, ReservationInfo reservationInfo);
}
