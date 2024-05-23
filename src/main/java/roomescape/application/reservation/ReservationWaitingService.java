package roomescape.application.reservation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationStatusResponse;

@Service
public class ReservationWaitingService {
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationWaitingService(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    public List<ReservationStatusResponse> findAllByMemberId(long memberId) {
        List<ReservationStatusResponse> reservationResponses = reservationService.findAllByMemberId(memberId);
        List<ReservationStatusResponse> waitingResponses = waitingService.findAllByMemberId(memberId);

        return Stream.concat(reservationResponses.stream(), waitingResponses.stream())
                .collect(Collectors.toList());
    }
}
