package roomescape.slot.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.slot.application.port.out.SlotRepository;
import roomescape.slot.domain.Slot;

@Repository
@RequiredArgsConstructor
public class JpaSlotRepository implements SlotRepository {
    private final SpringDataSlotRepository repository;

    @Override
    public Slot save(Slot slot) {
        return repository.save(slot);
    }

    @Override
    public Optional<Slot> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return repository.findByDateAndTime_IdAndTheme_Id(date, timeId, themeId);
    }

    @Override
    public Optional<Slot> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        return repository.existsByTime_Id(timeId);
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        return repository.existsByTheme_Id(themeId);
    }

    @Override
    public List<Slot> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EscapeRoomException(ErrorCode.SLOT_IN_USE, id);
        }
    }

    @Override
    public boolean existsByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId) {
        return repository.existsByDateAndTheme_IdAndTime_Id(date, themeId, timeId);
    }
}
