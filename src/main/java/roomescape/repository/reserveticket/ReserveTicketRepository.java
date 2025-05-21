package roomescape.repository.reserveticket;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.domain.reserveticket.ReserveTicket;

@org.springframework.stereotype.Repository
public interface ReserveTicketRepository extends Repository<ReserveTicket, Long> {

    ReserveTicket save(ReserveTicket reserveTicket);

    void deleteById(long id);

    List<ReserveTicket> findAllByReserverId(Long reserverId);

    List<ReserveTicket> findAll();

    //TODO 추후에 id로 검색하는 기능 추가하기
    @Query("""
                SELECT rt
                FROM ReserveTicket rt
                WHERE rt.reservation.date = :date
                  AND rt.reservation.time.id = :timeId
                  AND rt.reservation.theme.id = :themeId
            """)
    int countSameWaitingReservation(long themeId, LocalDate date, long timeId);
}
