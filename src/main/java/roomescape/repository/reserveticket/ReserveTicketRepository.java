package roomescape.repository.reserveticket;

import java.util.List;
import org.springframework.data.repository.Repository;
import roomescape.domain.reserveticket.ReserveTicket;

@org.springframework.stereotype.Repository
public interface ReserveTicketRepository extends Repository<ReserveTicket, Long> {

    ReserveTicket save(ReserveTicket reserveTicket);

    void deleteById(long id);

    List<ReserveTicket> findAllByReserverId(Long reserverId);

    List<ReserveTicket> findAll();
}
