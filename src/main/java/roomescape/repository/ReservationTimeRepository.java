package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    Optional<ReservationTime> findByStartAt(LocalTime startAt);

    @Query("""
            SELECT rt 
            FROM ReservationTime rt 
            INNER JOIN Reservation r 
            ON rt.id = r.reservationTime.id 
            WHERE r.date = :date 
            AND r.theme.id = :themeId
            """)
    List<ReservationTime> findReservationByThemeIdAndDate(@Param("date") LocalDate date,
                                                          @Param("themeId") long themeId);
}
