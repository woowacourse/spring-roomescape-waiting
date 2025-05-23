package roomescape.reservation.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.config.annotation.AuthMember;
import roomescape.exception.custom.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.reservation.controller.dto.response.MyReservationAndWaitingResponse;
import roomescape.reservation.entity.Reservation;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.service.WaitingService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class WaitingFacadeService {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public WaitingFacadeService(
            final ReservationService reservationService,
            final WaitingService waitingService
    ) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @Transactional(readOnly = true)
    public List<MyReservationAndWaitingResponse> readMyReservations(final @AuthMember Member member) {

        List<MyReservationAndWaitingResponse> responseFromReservation = reservationService.findReservationsByMemberId(member).stream()
                .map(MyReservationAndWaitingResponse::fromReservation)
                .toList();

        List<MyReservationAndWaitingResponse> responseFromWaiting = waitingService.findAllWaitingWithRankByMemberId(member).stream()
                .map(MyReservationAndWaitingResponse::fromWaitingWithRank)
                .toList();

        return Stream.concat(responseFromReservation.stream(), responseFromWaiting.stream())
                .toList();
    }

    @Transactional
    public void removeReservation(final long id) {
        Reservation reservation = reservationService.removeReservation(id);

        Optional<Waiting> waiting = waitingService.removeWaiting(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime()
        );
        waiting.ifPresent(w -> {
            reservationService.addReservation(
                    w.getMember(),
                    w.getDate(),
                    w.getTheme().getId(),
                    w.getTime().getId()
            );
        });
    }
}
