package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.service.dto.ReservationStatus;

import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class ReservationLookupService {
    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public ReservationLookupService(ReservationService reservationService,
                                    ReservationWaitingService reservationWaitingService) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    public List<ReservationStatus> findByName(String name) {
        return Stream.concat(
                reservationService.findByName(name).stream()
                        .map(ReservationStatus::reserved),
                reservationWaitingService.findByName(name).stream()
                        .map(ReservationStatus::waiting)
        ).toList();
    }
}
