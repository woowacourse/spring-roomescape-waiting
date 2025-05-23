package roomescape.reservation.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.application.dto.response.AdminReservationWaitingServiceResponse;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.ReservationWaitingRepository;

@Service
@RequiredArgsConstructor
public class AdminReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;

    public List<AdminReservationWaitingServiceResponse> getAll() {
        List<ReservationWaiting> reservationWaitings = reservationWaitingRepository.getAll();
        return reservationWaitings.stream()
                .map(AdminReservationWaitingServiceResponse::from)
                .toList();
    }
}
