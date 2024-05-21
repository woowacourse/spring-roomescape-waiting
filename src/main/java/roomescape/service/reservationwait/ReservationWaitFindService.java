package roomescape.service.reservationwait;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationWait;
import roomescape.repository.ReservationWaitRepository;

@Service
public class ReservationWaitFindService {

    private final ReservationWaitRepository reservationWaitRepository;

    public ReservationWaitFindService(ReservationWaitRepository reservationWaitRepository) {
        this.reservationWaitRepository = reservationWaitRepository;
    }

    public List<ReservationWait> findUserReservationWaits(long memberId) {
        return reservationWaitRepository.findByMemberId(memberId);
    }
}
