package roomescape.application.reservation;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.application.reservation.response.ReservationSlotResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationSlotService {

    private final ReservationSlotRepository slotRepository;

    public List<ReservationSlotResponse> getReservationSlots(Long themeId, LocalDate date) {
        return slotRepository.findWaitingCountsByThemeIdAndDate(themeId, date).stream()
                .map(ReservationSlotResponse::from)
                .toList();
    }
}
