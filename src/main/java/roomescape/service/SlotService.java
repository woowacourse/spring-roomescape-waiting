package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;
import roomescape.repository.ReservationTimeJpaRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeJpaRepository;

@Service
@Transactional(readOnly = true)
public class SlotService {
    private final SlotRepository slotRepository;
    private final ReservationTimeJpaRepository reservationTimeRepository;
    private final ThemeJpaRepository themeRepository;

    public SlotService(SlotRepository slotRepository, ReservationTimeJpaRepository reservationTimeRepository,
                       ThemeJpaRepository themeRepository) {
        this.slotRepository = slotRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Slot findOrCreate(LocalDate date, long timeId, long themeId) {
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new RoomEscapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ErrorCode.THEME_NOT_FOUND));

        return slotRepository.findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> saveOrReread(date, time, theme));
    }

    private Slot saveOrReread(LocalDate date, ReservationTime time, Theme theme) {
        try {
            return slotRepository.save(Slot.create(new ReservationDate(date), time, theme));
        } catch (DataIntegrityViolationException e) {
            return slotRepository.findByDateAndTimeAndTheme(date, time, theme)
                    .orElseThrow(() -> new RoomEscapeException(ErrorCode.SLOT_NOT_FOUND));
        }
    }

    public void lockSlot(Slot foundSlot) {
        if (!slotRepository.lockSlot(foundSlot)) {
            throw new RoomEscapeException(ErrorCode.SLOT_NOT_FOUND);
        }
    }
}
