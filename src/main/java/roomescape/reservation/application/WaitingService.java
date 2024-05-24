package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingRepository;
import roomescape.reservation.domain.WaitingWithRank;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WaitingService {
    private final WaitingRepository waitingRepository;

    public WaitingService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public Long findRankByReservation(Reservation reservation) {
        if (reservation.isBooked()) {
            return 0L;
        }
        WaitingWithRank waitingWithRank = waitingRepository.findByMember(reservation.getMember());
        return waitingWithRank.getRank() + 1;
    }

    public List<Waiting> findAll() {
        return waitingRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        waitingRepository.deleteById(id);
    }
}
