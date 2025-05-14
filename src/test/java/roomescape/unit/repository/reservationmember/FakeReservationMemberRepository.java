package roomescape.unit.repository.reservationmember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMember;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.repository.reservationmember.ReservationMemberRepository;

public class FakeReservationMemberRepository implements ReservationMemberRepository {

    private final AtomicLong index = new AtomicLong(1L);
    private final List<ReservationMember> reservationMembers = new ArrayList<>();

    @Override
    public List<ReservationMember> findAll() {
        return Collections.unmodifiableList(reservationMembers);
    }

    @Override
    public long add(Reservation reservation, Member member) {
        long id = index.getAndIncrement();
        reservationMembers.add(new ReservationMember(id, reservation, member));
        return id;
    }

    @Override
    public void deleteById(long id) {
        ReservationMember deleteReservationMember = reservationMembers.stream()
                .filter(reservationMember -> reservationMember.getId() == id)
                .findAny()
                .orElseThrow(() -> new InvalidReservationException("존재하지 않는 id입니다"));
        reservationMembers.remove(deleteReservationMember);
    }

    @Override
    public List<ReservationMember> findAllByMemberId(Long memberId) {
        return reservationMembers.stream()
                .filter(currentReservationMember -> currentReservationMember.getMemberId() == memberId)
                .collect(Collectors.toList());
    }
}
