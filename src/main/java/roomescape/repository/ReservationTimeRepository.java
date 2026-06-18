package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query(
            value = """
                    SELECT t FROM ReservationTime t
                    INNER JOIN Reservation r ON r.slot.time.id = t.id
                    WHERE r.slot.reservationDate = :reservationDate 
                      AND r.slot.theme.id = :themeId
                    """)
    List<ReservationTime> findReservedTimesByDateAndTheme_Id(
            @Param("reservationDate") LocalDate reservationDate,
            @Param("themeId") Long themeId
    );

    boolean existsByStartAt(LocalTime startAt);
}
