package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.infrastructure.ReservationWaitingRepository;
import roomescape.service.response.ReservationWaitingAppResponse;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    public List<ReservationWaitingAppResponse> findAll() {
        return reservationWaitingRepository.findAll().stream()
                .map(ReservationWaitingAppResponse::from)
                .toList();
    }

    public List<ReservationWaitingAppResponse> findAllByMemberId(Long memberId) {
        return reservationWaitingRepository.findAllByMemberId(memberId).stream()
                .map(ReservationWaitingAppResponse::from)
                .toList();
    }
}
