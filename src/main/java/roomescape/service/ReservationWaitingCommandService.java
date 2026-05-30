package roomescape.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.ReservationWaitingRequest;
import roomescape.repository.ReservationWaitingRepository;

@Service
public class ReservationWaitingCommandService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationQueryService reservationQueryService;
    private final ReservationWaitingQueryService reservationWaitingQueryService;

    public ReservationWaitingCommandService(
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationQueryService reservationQueryService,
            ReservationWaitingQueryService reservationWaitingQueryService
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationQueryService = reservationQueryService;
        this.reservationWaitingQueryService = reservationWaitingQueryService;
    }

    @Transactional
    public ReservationWaiting save(ReservationWaitingRequest request) {
        Reservation reservation = reservationQueryService.getById(request.reservationId());
        ReservationWaiting reservationWaiting = ReservationWaiting.createWith(
                request.name(),
                LocalDateTime.now(),
                reservation
        );

        return reservationWaitingRepository.save(reservationWaiting);
    }

    @Transactional
    public void deleteMine(Long id, String name) {
        ReservationWaiting reservationWaiting = reservationWaitingQueryService.getById(id);
        reservationWaiting.cancelBy(name);

        reservationWaitingRepository.deleteById(id);
    }
}
