package roomescape.service.reservationwait;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.InvalidRequestException;
import roomescape.repository.ReservationWaitRepository;

@Service
public class ReservationWaitDeleteService {

    private final ReservationWaitRepository reservationWaitRepository;

    public ReservationWaitDeleteService(ReservationWaitRepository reservationWaitRepository) {
        this.reservationWaitRepository = reservationWaitRepository;
    }

    @Transactional
    public void deleteById(long id) {
        reservationWaitRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 예약 대기입니다."));
        reservationWaitRepository.deleteById(id);
    }
}
