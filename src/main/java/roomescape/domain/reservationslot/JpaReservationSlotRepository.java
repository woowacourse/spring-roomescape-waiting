package roomescape.domain.reservationslot;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface JpaReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    int countByTimeId(Long id);

    int countByDateId(Long id);

    int countByThemeId(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select reservationSlot
        from ReservationSlot reservationSlot
        where reservationSlot.time.id = :timeId
          and reservationSlot.date.id = :dateId
          and reservationSlot.theme.id = :themeId
        """)
    Optional<ReservationSlot> findSlotForCreation(Long timeId, Long dateId, Long themeId);
}
