package roomescape.repository.reserveticket;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reserveticket.ReserveTicket;

public interface JpaReserveTicketRepository extends JpaRepository<ReserveTicket, Long> {

    @Query("SELECT rm FROM ReserveTicket rm WHERE rm.member.id = :memberId")
    List<ReserveTicket> findAllByMemberId(Long memberId);
}
