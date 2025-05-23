package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.ReservationWaitingRank;
import roomescape.domain.reservation.ReservationWaitingTicket;

public interface JpaReservationWaitingTicketRepository extends JpaRepository<ReservationWaitingTicket, Long> {

    ReservationWaitingTicket findByReservationId(Long reservationId);

    @Query(
            "select new roomescape.domain.reservation.ReservationWaitingRank(" +
                    "cast(count(r) + 1 as int)) " +
                    "from ReservationWaitingTicket rwt " +
                    "left join rwt.reservation r " +
                    "where rwt.createdAt < :createdAt " +
                    "and r.status = 'WAITING' " +
                    "and r.theme.id = :themeId " +
                    "and r.date = :date " +
                    "and r.time.id = :timeId"
    )
    ReservationWaitingRank countReservationWaitingsByThemeIdAndDateAndTimeIdAndCreatedAt(
            @Param("theme") Long themeId,
            @Param("date") LocalDate date,
            @Param("time") Long timeId,
            @Param("createdAt") LocalDateTime createdAt
    );
}
