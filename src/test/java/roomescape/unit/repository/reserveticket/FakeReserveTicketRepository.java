package roomescape.unit.repository.reserveticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.repository.reserveticket.ReserveTicketRepository;

public class FakeReserveTicketRepository implements ReserveTicketRepository {

    private final AtomicLong index = new AtomicLong(1L);
    private final List<ReserveTicket> reserveTickets = new ArrayList<>();

    @Override
    public List<ReserveTicket> findAll() {
        return Collections.unmodifiableList(reserveTickets);
    }

    @Override
    public long add(Reservation reservation, Member member) {
        long id = index.getAndIncrement();
        reserveTickets.add(new ReserveTicket(id, reservation, member));
        return id;
    }

    @Override
    public void deleteById(long id) {
        ReserveTicket deleteReserveTicket = reserveTickets.stream()
                .filter(reservationMember -> reservationMember.getId() == id)
                .findAny()
                .orElseThrow(() -> new InvalidReservationException("존재하지 않는 id입니다"));
        reserveTickets.remove(deleteReserveTicket);
    }

    @Override
    public List<ReserveTicket> findAllByMemberId(Long memberId) {
        return reserveTickets.stream()
                .filter(currentReservationMember -> currentReservationMember.getMemberId() == memberId)
                .collect(Collectors.toList());
    }
}
