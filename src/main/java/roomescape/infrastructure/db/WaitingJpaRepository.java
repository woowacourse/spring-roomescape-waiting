package roomescape.infrastructure.db;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.model.Waiting;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByReservation_MemberId(Long id);

    @Query("""
             SELECT COUNT(w) FROM Waiting w
             WHERE w.registeredAt < :registeredAt AND 
                     w.reservation.date = :date AND
                    w.reservation.theme = :theme AND 
                    w.reservation.reservationTime = :reservationTime
            """)
    int countWaitingBefore(
            @Param("registeredAt") LocalDateTime registeredAt,
            @Param("date") LocalDate date,
            @Param("theme") Theme theme,
            @Param("reservationTime") ReservationTime reservationTime
    );


    @Query("""
            SELECT w FROM Waiting w
                    WHERE  w.reservation.date = :date AND
                    w.reservation.reservationTime = :reservationTime AND
                    w.reservation.theme = :theme
            ORDER BY w.registeredAt ASC
            LIMIT 1
            """)
    Optional<Waiting> findEarliestWaitingBy(LocalDate date, ReservationTime reservationTime, Theme theme);
}

