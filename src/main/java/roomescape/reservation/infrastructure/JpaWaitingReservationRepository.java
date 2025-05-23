package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.util.List;

public interface JpaWaitingReservationRepository extends JpaRepository<WaitingReservation, Long> {

    List<WaitingReservation> findAllByUserId(Long userId);

    @Query("""
            SELECT COALESCE(MAX(w.waitingOrder), 0) FROM WaitingReservation w WHERE
                w.date = :date AND
                w.time = :time AND
                w.theme = :theme
            """)
    int findMaxWaitingByParams(ReservationDate date, ReservationTime time, Theme theme);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE WaitingReservation w 
            SET w.waitingOrder = w.waitingOrder - 1 
            WHERE w.time.id = :timeId 
              AND w.theme.id = :themeId 
              AND w.date = :date 
              AND w.waitingOrder > :waitingOrder
            """)
    int decrementWaitingOrderAfter(@Param("timeId") Long timeId,
                                   @Param("themeId") Long themeId,
                                   @Param("date") ReservationDate date,
                                   @Param("waitingOrder") int waitingOrder);

}

