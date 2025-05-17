package roomescape.repository.reserveticket;

import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reserveticket.ReserveTicket;

@Repository
public class ReserveTicketRepositoryImpl implements ReserveTicketRepository {

    private final JpaReserveTicketRepository jpaReserveTicketRepository;

    public ReserveTicketRepositoryImpl(JpaReserveTicketRepository jpaReserveTicketRepository) {
        this.jpaReserveTicketRepository = jpaReserveTicketRepository;
    }

    @Override
    public long add(Reservation reservation, Member member) {
        ReserveTicket reserveTicket = new ReserveTicket(null, reservation, member);
        return jpaReserveTicketRepository.save(reserveTicket).getId();
    }

    @Override
    public void deleteById(long id) {
        jpaReserveTicketRepository.deleteById(id);
    }

    @Override
    public List<ReserveTicket> findAllByMemberId(Long memberId) {
        return jpaReserveTicketRepository.findAllByMemberId(memberId);
    }

    @Override
    public List<ReserveTicket> findAll() {
        return jpaReserveTicketRepository.findAll();
    }
}
