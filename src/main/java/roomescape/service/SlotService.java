package roomescape.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

@Service
@Transactional(readOnly = true)
public class SlotService {

    private final SlotRepository slotRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;

    public SlotService(SlotRepository slotRepository, TimeSlotRepository timeSlotRepository,
                       ThemeRepository themeRepository) {
        this.slotRepository = slotRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Slot resolveSlot(Slot targetSlot) {
        return slotRepository.findByDateAndTimeIdAndThemeId(
                targetSlot.getDate(), targetSlot.getTimeSlot().getId(), targetSlot.getTheme().getId()
        ).orElseGet(() -> slotRepository.save(targetSlot));
    }

    public Slot resolveNewSlot(LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = findTimeSlotOrNull(timeId);
        Theme theme = findThemeOrNull(themeId);
        return resolveSlot(Slot.transientOf(date, timeSlot, theme));
    }

    public Slot findSlotOrNull(LocalDate date, Long timeId, Long themeId) {
        return slotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId).orElse(null);
    }

    @Transactional
    public void deleteSlot(long id) {
        slotRepository.deleteById(id);
    }

    public TimeSlot findTimeSlotOrNull(Long timeId) {
        if (timeId == null) {
            return null;
        }
        return timeSlotRepository.findById(timeId).orElseThrow(() -> new TimeSlotNotFoundException(timeId));
    }

    public Theme findThemeOrNull(Long themeId) {
        if (themeId == null) {
            return null;
        }
        return themeRepository.findById(themeId).orElseThrow(() -> new ThemeNotFoundException(themeId));
    }
}
