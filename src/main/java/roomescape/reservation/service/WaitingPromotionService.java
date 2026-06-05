package roomescape.reservation.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.waiting.service.WaitingService;

@Service
@Transactional
@RequiredArgsConstructor
public class WaitingPromotionService {

    private final WaitingService waitingService;
    private final ReservationService reservationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void promoteBySlot(final LocalDate reservationDate, final long timeId, final long themeId) {
        waitingService.findEarliestWaitingBySlot(
            reservationDate,
            timeId,
            themeId
        ).ifPresent(waiting -> {
            waitingService.deleteByIdForPromotion(waiting.getId());
            reservationService.create(
                waiting.getCustomerNameValue(),
                waiting.getReservationDate(),
                waiting.getTime(),
                waiting.getTheme()
            );
        });

    }
}
