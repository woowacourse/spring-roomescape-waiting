package roomescape.repository.reservationmember;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMemberIds;

@Repository
public class ReservationMemberRepositoryImpl implements ReservationMemberRepository {

    private final JpaReservationMemberRepository jpaReservationMemberRepository;

    public ReservationMemberRepositoryImpl(JpaReservationMemberRepository jpaReservationMemberRepository) {
        this.jpaReservationMemberRepository = jpaReservationMemberRepository;
    }

    @Override
    public long add(Reservation reservation, Member member) {
        ReservationMemberIds reservationMemberIds = new ReservationMemberIds(-1, reservation.getId(), member.getId());
        return jpaReservationMemberRepository.save(reservationMemberIds).getId();
    }

    @Override
    public void deleteById(long id) {
        jpaReservationMemberRepository.deleteById(id);
    }

    @Override
    public List<ReservationMemberIds> findAllByMemberId(Long memberId) {
        return jpaReservationMemberRepository.findAllByMemberId(memberId);
    }

    @Override
    public List<ReservationMemberIds> findAll() {
        return jpaReservationMemberRepository.findAll();
    }

    @Override
    public Optional<ReservationMemberIds> findById(long id) {
        return jpaReservationMemberRepository.findById(id);
    }
}
