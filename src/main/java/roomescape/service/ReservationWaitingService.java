package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationWaiting;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private static final String RESERVATION_WAITING_NOT_FOUND_FORMAT = "ID %d번 예약을 찾을 수 없습니다.";
    private static final String NOT_OWNER = "본인의 예약 대기가 아닙니다.";

    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public ReservationWaiting addWaiting(ReservationWaiting reservationWaiting) {
        return reservationWaitingRepository.save(reservationWaiting);
    }

    public boolean existBy(String name, Long reservationId) {
        return reservationWaitingRepository.existBy(name, reservationId);
    }

    @Transactional
    public void cancelMyReservationWaiting(Long id, String name) {

        ReservationWaiting reservationWaiting = findById(id);
        if (!reservationWaiting.isOwnedBy(name)) {
            throw new UnauthorizedException(NOT_OWNER);
        }

        reservationWaitingRepository.deleteById(id);
    }

    public ReservationWaiting findById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(RESERVATION_WAITING_NOT_FOUND_FORMAT, id)));
    }

}
