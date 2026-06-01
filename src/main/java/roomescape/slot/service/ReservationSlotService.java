package roomescape.slot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.date.domain.ReservationDate;
import roomescape.date.exception.ReservationDateException;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.repository.ReservationSlotRepository;
import roomescape.slot.service.dto.SlotSaveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.ReservationTimeException;
import roomescape.time.repository.ReservationTimeRepository;

import static roomescape.date.exception.ReservationDateErrorInformation.DATE_NOT_FOUND;
import static roomescape.theme.exception.ThemeErrorInformation.THEME_NOT_FOUND;
import static roomescape.time.exception.ReservationTimeErrorInformation.TIME_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReservationSlotService {

    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationDateRepository reservationDateRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationSlot save(SlotSaveCommand command) {
        ReservationDate date = getReservationDate(command.dateId());
        ReservationTime time = getReservationTime(command.timeId());
        Theme theme = getTheme(command.themeId());
        return reservationSlotRepository.save(ReservationSlot.of(date, time, theme));
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeException(TIME_NOT_FOUND));
    }

    private ReservationDate getReservationDate(Long dateId) {
        return reservationDateRepository.findById(dateId)
                .orElseThrow(() -> new ReservationDateException(DATE_NOT_FOUND));
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeException(THEME_NOT_FOUND));
    }

}
