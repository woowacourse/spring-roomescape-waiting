package roomescape.domain.reservationslot;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.dto.ReservationCountResult;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationslot.dto.ReservationSlotResponse;
import roomescape.domain.theme.ThemeRepository;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ReservationDateErrors;
import roomescape.support.exception.errors.ThemeErrors;

@Service
@RequiredArgsConstructor
public class ReservationSlotService {

    private final ThemeRepository themeRepository;
    private final ReservationDateRepository reservationDateRepository;
    private final ReservationRepository reservationRepository;

    public List<ReservationSlotResponse> getReservationSlot(Long themeId, Long dateId) {
        validateThemeAndDateExists(themeId, dateId);
        List<ReservationCountResult> reservationCountResults = reservationRepository.countReservation(themeId, dateId);

        return reservationCountResults.stream()
            .map(ReservationSlotResponse::from)
            .toList();
    }

    private void validateThemeAndDateExists(Long themeId, Long dateId) {
        themeRepository.findById(themeId)
            .orElseThrow(() -> new NotFoundException(ThemeErrors.THEME_NOT_EXIST));
        reservationDateRepository.findById(dateId)
            .orElseThrow(() -> new NotFoundException(ReservationDateErrors.RESERVATION_DATE_NOT_EXIST));
    }
}
