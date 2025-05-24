package roomescape.repository.reserveticket;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reserveticket.ReserveTicket;

public interface JpaReserveTicketRepository extends JpaRepository<ReserveTicket, Long> {

    @Query("SELECT rm FROM ReserveTicket rm WHERE rm.member.id = :memberId")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    List<ReserveTicket> findAllByMemberId(Long memberId);
}
