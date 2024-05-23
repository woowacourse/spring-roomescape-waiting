package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.WaitingRepository;
import roomescape.reservation.domain.WaitingWithRank;

@Service
@Transactional(readOnly = true)
public class WaitingService {
    private final WaitingRepository waitingRepository;

    public WaitingService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public Long findLankByReservation(Reservation reservation) {
        if (reservation.isBooked()) {
            return 0L;
        }
        WaitingWithRank waitingWithRank = waitingRepository.findByMember(reservation.getMember());
        return waitingWithRank.getRank() + 1;
    }

    @Transactional
    public void deleteById(Long id) {
        waitingRepository.deleteById(id);
    }
}
