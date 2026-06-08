package roomescape.application.reservation;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.response.ReservationSlotsResponse;
import roomescape.domain.reservation.ReservationSlotRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationSlotService {

    private final ReservationSlotRepository slotRepository;

    public ReservationSlotsResponse getReservationSlots(Long themeId, LocalDate date) {
        return ReservationSlotsResponse.from(slotRepository.findWaitingCountsByThemeIdAndDate(themeId, date));
    }
}
