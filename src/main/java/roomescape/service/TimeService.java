package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.ThemeSlotRepository;
import roomescape.repository.TimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeService {

    private final TimeRepository timeRepository;
    private final ThemeSlotRepository themeSlotRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public TimeService(
            TimeRepository timeRepository,
            ThemeSlotRepository themeSlotRepository,
            ThemeRepository themeRepository, ReservationRepository reservationRepository
    ) {
        this.timeRepository = timeRepository;
        this.themeSlotRepository = themeSlotRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Time> allTimes() {
        return timeRepository.findAll();
    }

    public Time saveTime(LocalTime startAt) {
        Time time = new Time(startAt);
        return timeRepository.save(time);
    }

    public void removeTime(long timeId) {
        getTimeOrElseThrow(timeId);
        if (reservationRepository.existsByTimeId(timeId)) {
            throw new CustomException(ErrorCode.TIME_IS_REFERENCED);
        }

        timeRepository.deleteById(timeId);
    }

    public Time findTime(long timeId) {
        return getTimeOrElseThrow(timeId);
    }

    public List<ThemeSlot> findThemeSlotBy(long themeId, LocalDate date) {
        boolean isExist = themeSlotRepository.isExistBy(themeId, date);
        if (isExist) {
            return themeSlotRepository.findByThemeIdAndDate(themeId, date);
        }

        Theme theme = getThemeOrElseThrow(themeId);
        List<Time> times = timeRepository.findAll();
        List<ThemeSlot> themeSlots = new ArrayList<>();
        times.forEach(time -> themeSlots.add(new ThemeSlot(theme, date, time, false)));
        return themeSlotRepository.saveAll(themeSlots);
    }

    private Theme getThemeOrElseThrow(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_NOT_FOUND));
    }

    private Time getTimeOrElseThrow(long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new CustomException(ErrorCode.TIME_NOT_FOUND));
    }
}
