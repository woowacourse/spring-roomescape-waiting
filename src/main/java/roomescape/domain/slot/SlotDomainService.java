package roomescape.domain.slot;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;

@Service
public class SlotDomainService {
    private final SlotRepository slotRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public SlotDomainService(SlotRepository slotRepository, ReservationTimeRepository reservationTimeRepository,
                             ThemeRepository themeRepository) {
        this.slotRepository = slotRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public void delete(Long id) {
        slotRepository.delete(id);
    }

    public Optional<Slot> find(LocalDate date, Long timeId, Long themeId) {
        return slotRepository.findByDateAndTimeAndTheme(date, timeId, themeId);
    }

    public boolean isExistByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return slotRepository.isExistByDateAndTimeAndTheme(date, timeId, themeId);
    }

    public Slot create(LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = reservationTimeRepository.findReservationTimeById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException(timeId));
        Theme theme = themeRepository.findThemeById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException(themeId));

        Slot slot = Slot.create(date, time, theme);

        if (slot.isExpired()) {
            throw new ExpiredDateTimeException();
        }

        return slotRepository.findByDateAndTimeAndTheme(date, timeId, themeId)
                .orElseGet(() -> slot.withId(slotRepository.insert(slot)));
    }
}
