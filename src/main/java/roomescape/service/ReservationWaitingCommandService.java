package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.api.dto.ReservationWaitingRequest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.ReservationWaitingRepository;

@Service
public class ReservationWaitingCommandService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationQueryService reservationQueryService;
    private final ReservationWaitingQueryService reservationWaitingQueryService;
    private final Clock clock;

    public ReservationWaitingCommandService(
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationQueryService reservationQueryService,
            ReservationWaitingQueryService reservationWaitingQueryService,
            Clock clock
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationQueryService = reservationQueryService;
        this.reservationWaitingQueryService = reservationWaitingQueryService;
        this.clock = clock;
    }

    @Transactional
    public ReservationWaiting save(ReservationWaitingRequest request) {
        Reservation reservation = reservationQueryService.getById(request.reservationId());
        ReservationWaiting reservationWaiting = ReservationWaiting.createWith(
                request.name(),
                LocalDateTime.now(clock),
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
