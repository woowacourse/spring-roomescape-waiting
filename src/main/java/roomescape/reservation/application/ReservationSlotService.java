package roomescape.reservation.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationCountResult;
import roomescape.reservation.domain.ReservationDateRepository;
import roomescape.reservation.presentation.response.ReservationSlotResponse;
import roomescape.theme.domain.ThemeRepository;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.errors.ReservationDateErrors;
import roomescape.common.exception.errors.ThemeErrors;

@Service
@RequiredArgsConstructor
public class ReservationSlotService {

    private final ThemeRepository themeRepository;
    private final ReservationDateRepository reservationDateRepository;
    private final ReservationRepository reservationRepository;

    public List<ReservationSlotResponse> getReservationSlots(Long themeId, Long dateId) {
        validateThemeAndDateExists(themeId, dateId);
        List<ReservationCountResult> waitingReservationCounts =
                reservationRepository.countWaitingReservationsByThemeAndDate(themeId, dateId);

        return waitingReservationCounts.stream()
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
