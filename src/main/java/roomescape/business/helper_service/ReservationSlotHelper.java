package roomescape.business.helper_service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.ReservationSlots;
import roomescape.business.model.repository.ReservationTimes;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.vo.Id;
import roomescape.exception.business.NotFoundException;

import java.time.LocalDate;
import java.util.List;

import static roomescape.exception.ErrorCode.RESERVATION_TIME_NOT_EXIST;
import static roomescape.exception.ErrorCode.THEME_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class ReservationSlotHelper {

    private final ReservationSlots slots;
    private final Themes themes;
    private final ReservationTimes reservationTimes;

    public List<ReservationSlot> getAllSlotsContainsReserverOf(final String userIdValue) {
        return slots.findAllSlotsContainsReserverOf(Id.create(userIdValue));
    }

    public ReservationSlot findByDateAndTimeIdAndThemeIdOrElseSave(final LocalDate date, final String timeIdValue, final String themeIdValue) {
        return slots.findByDateAndTimeIdAndThemeId(date, Id.create(timeIdValue), Id.create(themeIdValue))
                .orElseGet(() -> addAndGet(date, timeIdValue, themeIdValue));
    }

    private ReservationSlot addAndGet(final LocalDate date, final String reservationTimeIdValue, final String themeIdValue) {
        Theme theme = themes.findById(Id.create(themeIdValue))
                .orElseThrow(() -> new NotFoundException(THEME_NOT_EXIST));
        ReservationTime time = reservationTimes.findById(Id.create(reservationTimeIdValue))
                .orElseThrow(() -> new NotFoundException(RESERVATION_TIME_NOT_EXIST));

        ReservationSlot createdSlot = new ReservationSlot(time, date, theme);
        slots.save(createdSlot);
        return createdSlot;
    }
}
