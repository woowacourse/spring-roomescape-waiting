package roomescape.slot.adapter.out.persistence;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.slot.domain.Slot;

interface SpringDataSlotRepository extends JpaRepository<Slot, Long> {
    Optional<Slot> findByDateAndTime_IdAndTheme_Id(LocalDate date, long timeId, long themeId);

    boolean existsByTime_Id(long timeId);

    boolean existsByTheme_Id(long themeId);

    boolean existsByDateAndTheme_IdAndTime_Id(LocalDate date, long themeId, long timeId);
}
