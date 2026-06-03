package roomescape.service;

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

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class SlotService {

    private final SlotRepository slotRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;

    public SlotService(SlotRepository slotRepository, TimeSlotRepository timeSlotRepository, ThemeRepository themeRepository) {
        this.slotRepository = slotRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Slot resolveSlot(LocalDate date, Long timeId, Long themeId) {
        return slotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseGet(() -> createNewSlot(date, timeId, themeId));
    }

    public Slot findSlotOrNull(LocalDate date, Long timeId, Long themeId) {
        return slotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId).orElse(null);
    }

    private Slot createNewSlot(LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeId).orElseThrow(() -> new TimeSlotNotFoundException(timeId));
        Theme theme = themeRepository.findById(themeId).orElseThrow(() -> new ThemeNotFoundException(themeId));
        return slotRepository.save(Slot.transientOf(date, timeSlot, theme));
    }
}
