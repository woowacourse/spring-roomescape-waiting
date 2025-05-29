package roomescape.reservationslot.infrastructure;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservationslot.domain.ReservationSlot;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
            SELECT rs 
            FROM ReservationSlot rs                
            JOIN rs.time t 
            JOIN rs.theme th                   
            WHERE th.id = :themeId     
              AND t.id = :timeId
              AND rs.date = :date
            """)
    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
                SELECT COUNT(r) = 0 
                FROM ReservationSlot rs
                LEFT JOIN rs.reservations r
                WHERE rs.id = :slotId
            """)
    boolean isEmpty(Long slotId);
}
