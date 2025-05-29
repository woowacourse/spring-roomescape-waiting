package roomescape.application.reservationwaiting.controller;

import static java.util.stream.Stream.concat;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservationwaiting.controller.dto.MyReservationAndWaitingResponse;
import roomescape.application.reservationwaiting.service.ReservationWaitingService;
import roomescape.application.reservationwaiting.service.dto.ReservationInfoAndWaitingInfo;
import roomescape.member.service.dto.LoginMemberInfo;

@RestController
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationAndWaitingResponse>> findAllMyReservationAndWaiting(
            final LoginMemberInfo loginMemberInfo
    ) {
        ReservationInfoAndWaitingInfo myReservationAndWaiting = reservationWaitingService.findMyReservationAndWaiting(
                loginMemberInfo.id());
        Stream<MyReservationAndWaitingResponse> reservationResponses = myReservationAndWaiting.reservationInfos()
                .stream()
                .map(MyReservationAndWaitingResponse::new);
        Stream<MyReservationAndWaitingResponse> waitingResponses = myReservationAndWaiting.waitingInfos()
                .stream()
                .map(MyReservationAndWaitingResponse::new);
        List<MyReservationAndWaitingResponse> responses = concat(reservationResponses, waitingResponses).toList();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable("id") final long reservationId,
            final LoginMemberInfo loginMemberInfo
    ) {
        reservationWaitingService.cancelReservation(reservationId, loginMemberInfo);
        return ResponseEntity.noContent().build();
    }
}
