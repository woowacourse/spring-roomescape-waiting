package roomescape.infrastructure.db;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.model.Waiting;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByPendingReservation_MemberId(Long id);

    @Query("""
             SELECT COUNT(w) FROM Waiting w
             WHERE w.registeredAt < :registeredAt AND 
                     w.pendingReservation.date = :date AND
                    w.pendingReservation.theme = :theme AND 
                    w.pendingReservation.reservationTime = :reservationTime
            """)
    int countWaitingBefore(
            @Param("registeredAt") LocalDateTime registeredAt,
            @Param("date") LocalDate date,
            @Param("theme") Theme theme,
            @Param("reservationTime") ReservationTime reservationTime
    );

}

