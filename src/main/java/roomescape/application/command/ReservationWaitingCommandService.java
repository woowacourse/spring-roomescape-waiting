package roomescape.application.command;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.ReservationWaitingRepository;

@Service
public class ReservationWaitingCommandService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final Clock clock;

    public ReservationWaitingCommandService(
            ReservationWaitingRepository reservationWaitingRepository,
            Clock clock
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationWaiting save(String name, Reservation reservation) {
        ReservationWaiting reservationWaiting = ReservationWaiting.createWith(
                name,
                LocalDateTime.now(clock),
                reservation
        );

        return reservationWaitingRepository.save(reservationWaiting);
    }

    @Transactional
    public void deleteMine(ReservationWaiting reservationWaiting, String name) {
        reservationWaiting.cancelBy(name);

        reservationWaitingRepository.deleteById(reservationWaiting.getId());
    }
}
