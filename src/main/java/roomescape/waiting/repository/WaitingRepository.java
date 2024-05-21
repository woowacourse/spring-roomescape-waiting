package roomescape.waiting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.model.Reservation;
import roomescape.waiting.model.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByMemberIdAndReservationId(Long memberId, Long reservationId);

    // TODO: 대처하기
    boolean existsByMemberAndReservation(Member member, Reservation reservation);
}
