package roomescape.repository.reservationmember;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationmember.ReservationMemberIds;

@Repository
public interface JpaReservationMemberRepository extends JpaRepository<ReservationMemberIds, Long> {

    List<ReservationMemberIds> findAllByMemberId(Long memberId);

}
