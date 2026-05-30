package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationWaiting;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingQueryService {

    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingQueryService(
            ReservationWaitingRepository reservationWaitingRepository
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    public ReservationWaiting getById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지않는 예약대기입니다. Id: " + id));
    }

    public List<ReservationWaiting> findMine(String name) {
        return reservationWaitingRepository.findByName(name);
    }
}
