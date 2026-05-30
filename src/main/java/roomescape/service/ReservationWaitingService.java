package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.WaitingWithOrder;
import roomescape.exception.ConflictException;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private static final String RESERVATION_WAITING_NOT_FOUND_FORMAT = "ID %d번 예약을 찾을 수 없습니다.";
    private static final String NOT_OWNER = "본인의 예약 대기가 아닙니다.";
    private static final String ALREADY_BOOKED = "이미 대기 중인 예약입니다.";

    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public WaitingWithOrder addWaiting(ReservationWaiting reservationWaiting) {
        if (reservationWaitingRepository.existBy(reservationWaiting.getName(), reservationWaiting.getReservation().getId())) {
            throw new ConflictException(ALREADY_BOOKED);
        }
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

    public List<WaitingWithOrder> getMyReservationWaitings(String name) {
        return reservationWaitingRepository.findByName(name);
    }
}
