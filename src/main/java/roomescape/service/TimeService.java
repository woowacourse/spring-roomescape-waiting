package roomescape.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    public List<Time> allTimes() {
        return timeRepository.findAll();
    }

    @Transactional
    public Time saveTime(LocalTime startAt) {
        Time time = new Time(startAt);
        return timeRepository.save(time);
    }

    @Transactional
    public void removeTime(long timeId) {
        getTimeOrElseThrow(timeId);
        if (reservationRepository.existsByTimeId(timeId)) {
            throw new CustomException(ErrorCode.TIME_IS_REFERENCED);
        }

        timeRepository.deleteById(timeId);
    }

    @Transactional(readOnly = true)
    public List<ThemeSlot> findThemeSlotBy(long themeId, LocalDate date) {
        getThemeOrElseThrow(themeId);
        return themeSlotRepository.findByThemeIdAndDate(themeId, date);
    }

    @NonNull
    private Theme getThemeOrElseThrow(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_NOT_FOUND));
    }

    @NonNull
    private Time getTimeOrElseThrow(long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new CustomException(ErrorCode.TIME_NOT_FOUND));
    }
}
