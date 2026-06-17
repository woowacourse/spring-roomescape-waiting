package roomescape.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByName(String name);

    List<Reservation> findBySlot_Theme_IdAndSlot_Date(Long themeId, LocalDate date);

    boolean existsBySlot(ReservationSlot slot);

    boolean existsBySlotAndIdNot(ReservationSlot slot, Long id);

    boolean existsByNameAndSlot(String name, ReservationSlot slot);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slot.time.id = :timeId")
    boolean existsByTimeId(@Param("timeId") long timeId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slot.theme.id = :themeId")
    boolean existsByThemeId(@Param("themeId") long themeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findBySlotForUpdate(ReservationSlot slot);
}
