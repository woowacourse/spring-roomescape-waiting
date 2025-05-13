package roomescape.repository.reservationmember;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMemberIds;

@Repository
public interface JpaReservationMemberRepository extends JpaRepository<ReservationMemberIds, Long>,
        ReservationMemberRepository {

    long add(Reservation reservation, Member member);

    void deleteById(long id);

    List<ReservationMemberIds> findAllByMemberId(Long memberId);

    List<ReservationMemberIds> findAll();

    Optional<ReservationMemberIds> findById(long id);
}
