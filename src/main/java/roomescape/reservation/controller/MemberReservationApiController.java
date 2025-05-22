package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.resolver.LoginMember;
import roomescape.reservation.controller.request.ReservationRequest;
import roomescape.reservation.controller.request.WaitingCreateRequest;
import roomescape.reservation.controller.response.MemberReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.controller.response.WaitingResponse;
import roomescape.reservation.service.ReservationWaitingService;

@RestController
public class MemberReservationApiController {

    private final ReservationWaitingService reservationWaitingService;

    public MemberReservationApiController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@LoginMember MemberResponse memberResponse,
                                                                 @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationWaitingService.createReservationByName(memberResponse.name(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<ReservationResponse> responses = reservationWaitingService.findAllReservations();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(
            @LoginMember MemberResponse memberResponse) {
        List<MemberReservationResponse> reservations = reservationWaitingService.findAllReservationsByMemberId(memberResponse.id());
        List<MemberReservationResponse> waitings = reservationWaitingService.findWaitingsWithRankByMemberId(memberResponse.id());
        List<MemberReservationResponse> response = Stream.of(reservations, waitings)
                .flatMap(Collection::stream)
                .toList();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> create(@LoginMember MemberResponse memberResponse,
                                                  @RequestBody @Valid WaitingCreateRequest request) {
        WaitingResponse response = reservationWaitingService.createWaiting(memberResponse, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationWaitingService.deleteWaitingById(id);
        return ResponseEntity.noContent().build();
    }
}
