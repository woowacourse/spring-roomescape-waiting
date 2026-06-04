package roomescape.domain.reservationslot;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.dto.ReservationCountResult;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationslot.dto.ReservationSlotResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ReservationDateErrors;
import roomescape.support.exception.errors.ReservationSlotErrors;
import roomescape.support.exception.errors.ThemeErrors;

@Service
@RequiredArgsConstructor
public class ReservationSlotService {

    private final ReservationSlotRepository reservationSlotRepository;
    private final ThemeRepository themeRepository;
    private final ReservationDateRepository reservationDateRepository;
    private final ReservationRepository reservationRepository;

    public List<ReservationSlotResponse> getReservationSlots(Long themeId, Long dateId) {
        validateThemeAndDateExists(themeId, dateId);
        List<ReservationCountResult> reservationCountResults = reservationRepository.countReservation(themeId, dateId);

        return reservationCountResults.stream()
            .map(ReservationSlotResponse::from)
            .toList();
    }

    public ReservationSlot findOrCreateReservationSlot(
        ReservationDate reservationDate,
        ReservationTime reservationTime,
        Theme theme
    ) {
        try {
            return reservationSlotRepository.findBySchedule(
                reservationTime.getId(),
                reservationDate.getId(),
                theme.getId()
            ).orElseGet(() -> saveReservationSlot(reservationDate, reservationTime, theme));
        } catch (DuplicateKeyException e) {
            return reservationSlotRepository.findBySchedule(
                reservationTime.getId(),
                reservationDate.getId(),
                theme.getId()
            ).orElseThrow(() -> new NotFoundException(ReservationSlotErrors.RESERVATION_SLOT_NOT_FOUND));
        }
    }

    private ReservationSlot saveReservationSlot(
        ReservationDate reservationDate,
        ReservationTime reservationTime,
        Theme theme
    ) {
        return reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme));
    }

    private void validateThemeAndDateExists(Long themeId, Long dateId) {
        themeRepository.findById(themeId)
            .orElseThrow(() -> new NotFoundException(ThemeErrors.THEME_NOT_EXIST));
        reservationDateRepository.findById(dateId)
            .orElseThrow(() -> new NotFoundException(ReservationDateErrors.RESERVATION_DATE_NOT_EXIST));
    }

    public void deleteReservationSlot(Long id) {
        reservationSlotRepository.deleteById(id);
    }
}
