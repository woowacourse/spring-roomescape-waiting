package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.ReservationWaitingRank;
import roomescape.domain.reservation.ReservationWaitingTicket;

public interface JpaReservationWaitingTicketRepository extends JpaRepository<ReservationWaitingTicket, Long> {

    Optional<ReservationWaitingTicket> findByReservationId(Long reservationId);

    void deleteByReservationId(Long reservationId);

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
            @Param("themeId") Long themeId,
            @Param("date") LocalDate date,
            @Param("timeId") Long timeId,
            @Param("createdAt") LocalDateTime createdAt
    );
}
