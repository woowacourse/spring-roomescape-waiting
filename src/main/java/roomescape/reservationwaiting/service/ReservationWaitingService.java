package roomescape.reservationwaiting.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.waiting.service.dto.WaitingInfo;

@Service
public class ReservationWaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationWaitingService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationInfo> findMyReservations(long memberId) {
        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(ReservationInfo::new)
                .toList();
    }

    public List<WaitingInfo> findMyWaiting(long memberId) {
        return waitingRepository.findAllByMemberId(memberId)
                .stream()
                .map(WaitingInfo::new)
                .toList();
    }
}
