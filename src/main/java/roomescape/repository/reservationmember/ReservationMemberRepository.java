package roomescape.repository.reservationmember;

import java.util.List;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMember;

public interface ReservationMemberRepository {

    long add(Reservation reservation, Member member);

    void deleteById(long id);

    List<ReservationMember> findAllByMemberId(Long memberId);

    List<ReservationMember> findAll();
}
