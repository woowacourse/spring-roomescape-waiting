package roomescape.waiting.repository;

import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.model.Reservation;
import roomescape.waiting.model.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    default Waiting getById(Long id) {
        return findById(id).orElseThrow(
                () -> new NoSuchElementException("식별자 " + id + "에 해당하는 예약 대기가 존재하지 않습니다."));
    }

    boolean existsByMemberIdAndReservationId(Long memberId, Long reservationId);

    // TODO: 대처하기
    boolean existsByMemberAndReservation(Member member, Reservation reservation);
}
