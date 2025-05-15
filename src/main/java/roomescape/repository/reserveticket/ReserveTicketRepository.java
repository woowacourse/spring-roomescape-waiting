package roomescape.repository.reserveticket;

import java.util.List;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reserveticket.ReserveTicket;

public interface ReserveTicketRepository {

    long add(Reservation reservation, Member member);

    void deleteById(long id);

    List<ReserveTicket> findAllByMemberId(Long memberId);

    List<ReserveTicket> findAll();
}
