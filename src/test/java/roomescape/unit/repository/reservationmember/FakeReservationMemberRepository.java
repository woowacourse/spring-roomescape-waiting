package roomescape.unit.repository.reservationmember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMemberIds;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.repository.reservationmember.ReservationMemberRepository;

public class FakeReservationMemberRepository implements ReservationMemberRepository {

    private final AtomicLong index = new AtomicLong(1L);
    private final List<ReservationMemberIds> reservationMemberIds = new ArrayList<>();

    @Override
    public List<ReservationMemberIds> findAll() {
        return Collections.unmodifiableList(reservationMemberIds);
    }

    @Override
    public Optional<ReservationMemberIds> findById(long id) {
        return reservationMemberIds.stream()
                .filter((currentReservationMemberIds -> currentReservationMemberIds.getId() == id))
                .findAny();
    }

    @Override
    public long add(Reservation reservation, Member member) {
        long id = index.getAndIncrement();
        reservationMemberIds.add(new ReservationMemberIds(id, reservation.getId(), member.getId()));
        return id;
    }

    @Override
    public void deleteById(long id) {
        ReservationMemberIds deleteReservationMemberId = reservationMemberIds.stream()
                .filter(reservationMemberIds -> reservationMemberIds.getId() == id)
                .findAny()
                .orElseThrow(() -> new InvalidReservationException("존재하지 않는 id입니다"));
        reservationMemberIds.remove(deleteReservationMemberId);
    }

    @Override
    public List<ReservationMemberIds> findAllByMemberId(Long memberId) {
        return reservationMemberIds.stream()
                .filter(currentReservationMemberIds -> currentReservationMemberIds.getMemberId() == memberId)
                .collect(Collectors.toList());
    }
}
