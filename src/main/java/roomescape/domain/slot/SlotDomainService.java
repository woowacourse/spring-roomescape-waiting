package roomescape.domain.slot;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.SlotDao;
import roomescape.repository.ThemeQueryingDao;

@Service
public class SlotDomainService {
    private final SlotDao slotDao;
    private final ReservationTimeQueryingDao reservationTimeQueryingDao;
    private final ThemeQueryingDao themeQueryingDao;

    public SlotDomainService(SlotDao slotDao, ReservationTimeQueryingDao reservationTimeQueryingDao, ThemeQueryingDao themeQueryingDao) {
        this.slotDao = slotDao;
        this.reservationTimeQueryingDao = reservationTimeQueryingDao;
        this.themeQueryingDao = themeQueryingDao;
    }

    public void delete(Long id) {
        slotDao.delete(id);
    }

    public Optional<Slot> find(LocalDate date, Long timeId, Long themeId) {
        return slotDao.findByDateAndTimeAndTheme(date, timeId, themeId);
    }

    public boolean isExistByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return slotDao.isExistByDateAndTimeAndTheme(date, timeId, themeId);
    }

    public Slot create(LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = reservationTimeQueryingDao.findReservationTimeById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException(timeId));
        Theme theme = themeQueryingDao.findThemeById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException(themeId));

        Slot slot = Slot.create(date, time, theme);

        if (slot.isExpired()) {
            throw new ExpiredDateTimeException();
        }


        return slotDao.findByDateAndTimeAndTheme(date, timeId, themeId)
                .orElseGet(() -> slot.withId(slotDao.insert(slot)));
    }
}
