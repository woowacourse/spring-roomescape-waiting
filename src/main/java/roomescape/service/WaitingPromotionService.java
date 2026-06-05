package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationStatus;
import roomescape.repository.ReservationRepository;

@Service
public class WaitingPromotionService {

    private final ReservationRepository reservationRepository;

    public WaitingPromotionService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void promoteFirstWaiting(Long slotId) {
        if (reservationRepository.existsBySlotIdAndStatus(slotId, ReservationStatus.RESERVED)) {
            return;
        }
        reservationRepository.findFirstWaitingBySlotId(slotId)
                .ifPresent(waiting -> reservationRepository.updateStatus(waiting.getId(), ReservationStatus.RESERVED));
    }
}
