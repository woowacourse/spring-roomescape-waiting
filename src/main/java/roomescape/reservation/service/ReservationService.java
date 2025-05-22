package roomescape.reservation.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.waiting.service.WaitingQueryService;
import roomescape.waiting.service.WaitingService;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationManager reservationManager;
    private final ReservedQueryService reservedQueryService;
    private final WaitingService waitingService;
    private final WaitingQueryService waitingQueryService;
    private final ReservationValidator reservationValidator;

    @Transactional
    public ReservationResponse reserve(ReserveCommand reserveCommand) {
        Reservation reserved = reservationManager.reserve(reserveCommand);

        return ReservationResponse.from(reserved);
    }

    @Transactional
    public ReservationResponse waiting(ReserveCommand reserveCommand) {
        reservationValidator.validateAvailableWaiting(reserveCommand);

        Reservation waiting = reservationManager.waiting(reserveCommand);

        return ReservationResponse.from(waiting);
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = reservedQueryService.getReserved(id);
        waitingService.promoteFirstWaitingToReservation(reservation.getDate(), reservation.getTimeId());
        reservationManager.delete(reservation);
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> getAllReservations(Long memberId) {
        List<MyReservationResponse> responses = new ArrayList<>();

        responses.addAll(reservedQueryService.getReservations(memberId));
        responses.addAll(waitingQueryService.getWaitingReservations(memberId));

        return responses;
    }
}
