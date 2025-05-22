package roomescape.repository.reserveticket;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reserveticket.ReserveTicket;

@org.springframework.stereotype.Repository
public interface ReserveTicketRepository extends Repository<ReserveTicket, Long> {

    ReserveTicket save(ReserveTicket reserveTicket);

    void deleteById(long id);

    List<ReserveTicket> findAllByReserverId(Long reserverId);

    List<ReserveTicket> findAll();

    @Query("""
                SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END
                FROM ReserveTicket rt
                WHERE rt.reservation.date = :date
                  AND rt.reservation.time.id = :timeId
                  AND rt.reservation.theme.id = :themeId
                  AND rt.reserver.id = :reserverId
                  AND rt.reservation.reservationStatus = :reservationStatus
            """)
    boolean existsBySameReservation(long themeId, LocalDate date, long timeId, long reserverId,
                                    ReservationStatus reservationStatus);
}
