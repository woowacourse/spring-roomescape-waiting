package roomescape.application;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.command.ReservationCommandService;
import roomescape.application.command.ReservationWaitingCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationWaitingQueryService;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Slot;

@Service
public class ReservationCancellationUseCase {

    private final ReservationCommandService reservationCommandService;
    private final ReservationWaitingCommandService reservationWaitingCommandService;
    private final ReservationWaitingQueryService reservationWaitingQueryService;
    private final ReservationQueryService reservationQueryService;

    public ReservationCancellationUseCase(
            ReservationCommandService reservationCommandService,
            ReservationWaitingCommandService reservationWaitingCommandService,
            ReservationWaitingQueryService reservationWaitingQueryService,
            ReservationQueryService reservationQueryService
    ) {
        this.reservationCommandService = reservationCommandService;
        this.reservationWaitingCommandService = reservationWaitingCommandService;
        this.reservationWaitingQueryService = reservationWaitingQueryService;
        this.reservationQueryService = reservationQueryService;
    }

    @Transactional
    public void delete(Long id) {
        reservationCommandService.delete(id);
    }

    @Transactional
    public void deleteMine(Long id, String name) {
        Reservation reservation = reservationQueryService.getById(id);
        Member requester = new Member(name);
        Slot slot = reservation.getSlot();

        Optional<ReservationWaiting> firstWaiting = reservationWaitingQueryService.findFirstBySlot(slot);
        if (firstWaiting.isEmpty()) {
            reservationCommandService.deleteMine(
                    reservation,
                    requester
            );
            return;
        }

        ReservationWaiting waiting = firstWaiting.get();
        reservationCommandService.changeReserver(
                reservation,
                requester,
                waiting.getWaiter()
        );
        reservationWaitingCommandService.delete(waiting);
    }

}
