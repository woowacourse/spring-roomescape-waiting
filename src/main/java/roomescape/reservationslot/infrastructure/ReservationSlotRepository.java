package roomescape.reservationslot.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
            SELECT r 
            FROM ReservationSlot r                
            JOIN r.time t 
            JOIN r.theme th                   
            WHERE th.id = :themeId     
              AND t.id = :timeId
              AND r.date = :date
            """)
    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
                SELECT CASE WHEN SIZE(rs.reservations) = 1 THEN TRUE ELSE FALSE END 
                FROM ReservationSlot  rs
                WHERE rs.id = :slotId
            """)
    boolean hasOnlyOneReservation(Long slotId);
}
