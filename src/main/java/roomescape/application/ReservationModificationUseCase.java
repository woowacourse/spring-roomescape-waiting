package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.command.ReservationCommandService;
import roomescape.application.command.ReservationWaitingCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ReservationWaitingQueryService;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.exception.ConflictException;
import roomescape.presentation.dto.ReservationUpdateRequest;

@Service
public class ReservationModificationUseCase {

    public static final String CANNOT_MOVE_TO_RESERVED_SLOT = "이미 예약이 있는 시간으로는 예약을 옮길 수 없습니다.";

    private final ReservationCommandService reservationCommandService;
    private final ReservationWaitingCommandService reservationWaitingCommandService;

    private final ReservationQueryService reservationQueryService;
    private final ReservationWaitingQueryService reservationWaitingQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;

    public ReservationModificationUseCase(
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
    public void deleteReservation(Long id) {
        Reservation reservation = reservationCommandService.getByIdForUpdate(id);
        Slot slot = reservation.getSlot();
        reservationCommandService.delete(reservation);

        promoteFirstWaitingToReservation(slot);
    }

    @Transactional
    public void deleteMyReservation(Long id, String name) {
        Reservation reservation = reservationCommandService.getByIdForUpdate(id);
        Member requester = new Member(name);
        Slot slot = reservation.getSlot();
        reservationCommandService.deleteMine(
                reservation,
                requester
        );

        promoteFirstWaitingToReservation(slot);
    }

    @Transactional
    public Reservation updateMyReservation(Long id, String name, ReservationUpdateRequest request) {
        Reservation existing = reservationCommandService.getByIdForUpdate(id);
        Slot sourceSlot = existing.getSlot();
        ReservationTime targetTime = reservationTimeQueryService.getById(request.timeId());
        Slot targetSlot = new Slot(
                request.date(),
                targetTime,
                existing.getTheme()
        );

        validateSlotConflict(existing, targetSlot);
        Reservation reservation = reservationCommandService.updateMine(
                existing,
                new Member(name),
                targetSlot
        );

        promoteFirstWaitingToReservation(sourceSlot);
        return reservation;
    }

    private void promoteFirstWaitingToReservation(Slot slot) {
        reservationWaitingQueryService.findFirstBySlot(slot)
                .ifPresent(waiting -> {
                    reservationCommandService.promote(waiting);
                    reservationWaitingCommandService.delete(waiting);
                });
    }

    private void validateSlotConflict(Reservation existing, Slot targetSlot) {
        reservationQueryService.findBySlot(targetSlot)
                .filter(found -> !found.equals(existing))
                .ifPresent(found -> {
                    throw new ConflictException(CANNOT_MOVE_TO_RESERVED_SLOT);
                });
    }
}
