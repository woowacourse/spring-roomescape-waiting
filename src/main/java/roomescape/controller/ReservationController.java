package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationService;
import roomescape.service.dto.AuthInfo;
import roomescape.service.dto.request.ReservationCreateMemberRequest;
import roomescape.service.dto.request.ReservationCreateRequest;
import roomescape.service.dto.response.ListResponse;
import roomescape.service.dto.response.MyReservationResponse;
import roomescape.service.dto.response.ReservationResponse;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<ListResponse<ReservationResponse>> findAllReservations() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<ListResponse<MyReservationResponse>> findMyReservations(AuthInfo authInfo) {
        return ResponseEntity.ok(reservationService.findMyReservations(authInfo));
    }

    @PostMapping({"/reservations"})
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationCreateMemberRequest memberRequest, AuthInfo authInfo
    ) {
        ReservationCreateRequest reservationCreateRequest = ReservationCreateRequest.from(memberRequest, authInfo.id());
        ReservationResponse reservationResponse = reservationService.save(reservationCreateRequest);
        return ResponseEntity.created(URI.create("/reservations" + reservationResponse.id())).body(reservationResponse);
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<ReservationResponse> createWaiting(
            @Valid @RequestBody ReservationCreateMemberRequest memberRequest, AuthInfo authInfo) {
        ReservationCreateRequest reservationCreateRequest = ReservationCreateRequest.from(memberRequest, authInfo.id());
        ReservationResponse reservationResponse = reservationService.wait(reservationCreateRequest);
        return ResponseEntity.created(URI.create("/reservations" + reservationResponse.id())).body(reservationResponse);
    }
}
