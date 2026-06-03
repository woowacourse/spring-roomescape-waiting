package roomescape.service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.SlotDao;
import roomescape.repository.ThemeQueryingDao;

@Service
public class SlotService {
    private final SlotDao slotDao;
    private final ReservationTimeQueryingDao reservationTimeQueryingDao;
    private final ThemeQueryingDao themeQueryingDao;

    public SlotService(SlotDao slotDao, ReservationTimeQueryingDao reservationTimeQueryingDao, ThemeQueryingDao themeQueryingDao) {
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

    public Slot getOrCreate(ReservationRequest request) {
        ReservationTime time = reservationTimeQueryingDao.findReservationTimeById(request.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(request.timeId()));
        Theme theme = themeQueryingDao.findThemeById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        Slot slot = Slot.create(request.date(), time, theme);
        if (slot.isExpired()) {
            throw new ExpiredDateTimeException();
        }

        return slotDao.findByDateAndTimeAndTheme(request.date(), request.timeId(), request.themeId())
                .orElseGet(() -> slot.withId(slotDao.insert(slot)));
    }
}
