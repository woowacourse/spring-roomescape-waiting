package roomescape.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Slot;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    @Query("""
        SELECT s
        FROM Slot s
        JOIN FETCH s.time
        JOIN FETCH s.theme
        WHERE s.date = :date
          AND s.time.id = :timeId
          AND s.theme.id = :themeId
        """)
    Optional<Slot> findByDateAndTimeIdAndThemeId(
        @Param("date") LocalDate date,
        @Param("timeId") Long timeId,
        @Param("themeId") Long themeId
    );

    @Query(value = "SELECT * FROM slot WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);

    default Slot getOrCreate(Slot slot) {
        if (slot.getId() != null) {
            return slot;
        }

        return findByDateAndTimeIdAndThemeId(
            slot.getDate(),
            slot.getTimeId(),
            slot.getThemeId()
        ).orElseGet(() -> save(slot));
    }

    default void lockById(Long id) {
        findByIdForUpdate(id).orElseThrow();
    }
}
