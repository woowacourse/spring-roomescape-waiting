package roomescape.application.command;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.ReservationWaitingRepository;

@Service
@Transactional
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

    public ReservationWaiting save(Member waiter, Reservation reservation) {
        ReservationWaiting reservationWaiting = ReservationWaiting.createWith(
                waiter,
                reservation.getReserver(),
                reservation.getSlot(),
                LocalDateTime.now(clock)
        );

        return reservationWaitingRepository.save(reservationWaiting);
    }

    public void delete(ReservationWaiting reservationWaiting) {
        reservationWaitingRepository.deleteById(reservationWaiting.getId());
        reservationWaitingRepository.flush();
    }

    public void deleteMine(ReservationWaiting reservationWaiting, Member requester) {
        reservationWaiting.cancelBy(requester);

        reservationWaitingRepository.deleteById(reservationWaiting.getId());
        reservationWaitingRepository.flush();
    }
}
