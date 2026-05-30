package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationWaiting;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private static final String RESERVATION_WAITING_NOT_FOUND_FORMAT = "ID %d번 예약을 찾을 수 없습니다.";

    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public ReservationWaiting addWaiting(ReservationWaiting reservationWaiting) {
        return reservationWaitingRepository.save(reservationWaiting);
    }

    @Transactional
    public void cancelMyReservationWaiting(Long id, String name) {
        ReservationWaiting reservationWaiting = getById(id);
        reservationWaiting.cancelBy(name);

        reservationWaitingRepository.deleteById(id);
    }

    public ReservationWaiting getById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(RESERVATION_WAITING_NOT_FOUND_FORMAT, id)));
    }

    public List<ReservationWaiting> getMyReservationWaitings(String name) {
        return reservationWaitingRepository.findByName(name);
    }
}
