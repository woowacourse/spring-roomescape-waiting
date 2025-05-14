package roomescape.repository.reservationmember;

import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMember;

@Repository
public class ReservationMemberRepositoryImpl implements ReservationMemberRepository {

    private final JpaReservationMemberRepository jpaReservationMemberRepository;

    public ReservationMemberRepositoryImpl(JpaReservationMemberRepository jpaReservationMemberRepository) {
        this.jpaReservationMemberRepository = jpaReservationMemberRepository;
    }

    @Override
    public long add(Reservation reservation, Member member) {
        ReservationMember reservationMember = new ReservationMember(null, reservation, member);
        return jpaReservationMemberRepository.save(reservationMember).getId();
    }

    @Override
    public void deleteById(long id) {
        jpaReservationMemberRepository.deleteById(id);
    }

    @Override
    public List<ReservationMember> findAllByMemberId(Long memberId) {
        return jpaReservationMemberRepository.findAllByMemberId(memberId);
    }

    @Override
    public List<ReservationMember> findAll() {
        return jpaReservationMemberRepository.findAll();
    }
}
