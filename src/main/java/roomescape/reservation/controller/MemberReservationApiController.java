package roomescape.reservation.controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.resolver.LoginMember;
import roomescape.reservation.controller.request.ReservationRequest;
import roomescape.reservation.controller.response.MemberReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingService;

@RestController
public class MemberReservationApiController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public MemberReservationApiController(ReservationService reservationService, final WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@LoginMember MemberResponse memberResponse,
                                                                 @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createByName(memberResponse.name(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<ReservationResponse> responses = reservationService.findAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(
            @LoginMember MemberResponse memberResponse) {
        List<MemberReservationResponse> reservations = reservationService.findAllByMemberId(memberResponse.id());
        List<MemberReservationResponse> waitings = waitingService.findAllByMemberId(memberResponse.id());
        List<MemberReservationResponse> response = Stream.of(reservations, waitings)
                .flatMap(Collection::stream)
                .toList();
        return ResponseEntity.ok().body(response);
    }
}
