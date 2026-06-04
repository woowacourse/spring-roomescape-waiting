package roomescape.application;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.api.dto.ReservationUpdateRequest;
import roomescape.application.command.ReservationCommandService;
import roomescape.application.command.ReservationWaitingCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ReservationWaitingQueryService;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Slot;

@Service
public class ReservationCancellationUseCase {

    private final ReservationCommandService reservationCommandService;
    private final ReservationWaitingCommandService reservationWaitingCommandService;

    private final ReservationQueryService reservationQueryService;
    private final ReservationWaitingQueryService reservationWaitingQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;

    public ReservationCancellationUseCase(
            ReservationCommandService reservationCommandService,
            ReservationWaitingCommandService reservationWaitingCommandService,
            ReservationQueryService reservationQueryService,
            ReservationWaitingQueryService reservationWaitingQueryService,
            ReservationTimeQueryService reservationTimeQueryService
    ) {
        this.reservationCommandService = reservationCommandService;
        this.reservationWaitingCommandService = reservationWaitingCommandService;
        this.reservationQueryService = reservationQueryService;
        this.reservationWaitingQueryService = reservationWaitingQueryService;
        this.reservationTimeQueryService = reservationTimeQueryService;
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = reservationQueryService.getById(id);
        Slot slot = reservation.getSlot();
        reservationCommandService.delete(reservation);

        promoteFirstWaitingToReservation(slot);
    }

    @Transactional
    public void deleteMine(Long id, String name) {
        Reservation reservation = reservationQueryService.getById(id);
        Member requester = new Member(name);
        Slot slot = reservation.getSlot();
        reservationCommandService.deleteMine(
                reservation,
                requester
        );

        promoteFirstWaitingToReservation(slot);
    }

    @Transactional
    public Reservation updateMine(Long id, String name, ReservationUpdateRequest request) {
        Reservation existing = reservationQueryService.getById(id);
        Slot sourceSlot = existing.getSlot();
        ReservationTime targetTime = reservationTimeQueryService.getById(request.timeId());
        Slot targetSlot = new Slot(
                request.date(),
                targetTime,
                existing.getTheme()
        );
        Reservation reservation = reservationCommandService.updateMine(
                existing,
                new Member(name),
                targetSlot
        );

        promoteFirstWaitingToReservation(sourceSlot);
        return reservation;
    }

    private void promoteFirstWaitingToReservation(Slot slot) {
        Optional<ReservationWaiting> firstWaiting = reservationWaitingQueryService.findFirstBySlot(slot);
        if (firstWaiting.isPresent()) {
            ReservationWaiting waiting = firstWaiting.get();
            reservationCommandService.save(
                    waiting.getWaiter(),
                    slot
            );
            reservationWaitingCommandService.delete(waiting);
        }
    }

}
