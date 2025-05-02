package roomescape.repository.reservationmember;

import java.util.List;
import java.util.Optional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMemberIds;

public interface ReservationMemberRepository {

    long add(Reservation reservation, Member member);

    void deleteById(long id);

    List<ReservationMemberIds> findAllByMemberId(Long memberId);

    List<ReservationMemberIds> findAll();

    Optional<ReservationMemberIds> findById(long id);
}
