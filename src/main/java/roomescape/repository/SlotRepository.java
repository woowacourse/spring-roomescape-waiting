package roomescape.repository;

import roomescape.domain.Slot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SlotRepository {

    List<Slot> findAll();

    Slot save(Slot slot);

    Optional<Slot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void deleteById(long id);
}
