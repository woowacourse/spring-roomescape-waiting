package roomescape.repository.reservationmember;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservationmember.ReservationMember;

@Repository
public interface JpaReservationMemberRepository extends JpaRepository<ReservationMember, Long> {

    List<ReservationMember> findAllByMember(Member member);

    @Query("SELECT rm FROM ReservationMember rm WHERE rm.member.id = :memberId")
    List<ReservationMember> findAllByMemberId(Long memberId);
}
