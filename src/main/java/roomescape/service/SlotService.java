package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public Slot findOrCreate(LocalDate date, long timeId, long themeId) {
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new RoomEscapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ErrorCode.THEME_NOT_FOUND));

        return slotRepository.findByDateAndTimeAndTheme(new ReservationDate(date), time, theme)
                .orElseGet(() -> saveOrReread(date, time, theme));
    }

    private Slot saveOrReread(LocalDate date, ReservationTime time, Theme theme) {
        try {
            return slotRepository.save(Slot.create(new ReservationDate(date), time, theme));
        } catch (DataIntegrityViolationException e) {
            return slotRepository.findByDateAndTimeAndTheme(new ReservationDate(date), time, theme)
                    .orElseThrow(() -> new RoomEscapeException(ErrorCode.SLOT_NOT_FOUND));
        }
    }
}
